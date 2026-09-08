package com.moait.moai.domain.couple.service;

import com.moait.moai.common.enums.CoupleStatus;
import com.moait.moai.common.enums.Gender;
import com.moait.moai.common.enums.InvitationStatus;
import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.couple.dto.CoupleConnectResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleDisconnectResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleMeResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleStatusResponseDTO;
import com.moait.moai.domain.couple.dto.InviteCodeResponseDTO;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.entity.Invitation;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.couple.repository.InvitationRepository;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.UserRepository;
import com.moait.moai.domain.user.service.OnboardingStepResolver;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoupleServiceImpl implements CoupleService {

    private static final String INVITE_URL_PREFIX = "https://moait.app/invite/";

    private final CoupleRepository coupleRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final OnboardingStepResolver onboardingStepResolver;

    @Override
    @Transactional(readOnly = true)
    public InviteCodeResponseDTO getInviteCode(Long userId) {
        Invitation master = invitationRepository.findByInviterIdAndInviteeIdIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_NOT_FOUND));
        String code = master.getInviteCode();
        return InviteCodeResponseDTO.of(code, INVITE_URL_PREFIX + code);
    }

    @Override
    @Transactional
    public CoupleConnectResponseDTO connect(Long userId, String inviteCode) {
        User me = findUser(userId);
        Invitation master = invitationRepository.findByInviteCodeAndInviteeIdIsNull(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_NOT_FOUND));

        Long partnerId = master.getInviterId();
        if (partnerId.equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_CONNECT_SELF);
        }
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_NOT_FOUND));
        if (me.getGender() == partner.getGender()) {
            throw new BusinessException(ErrorCode.SAME_GENDER);
        }
        assertNotConnectedElsewhere(userId, partnerId);
        assertNotConnectedElsewhere(partnerId, userId);

        long maleId = me.getGender() == Gender.MALE ? userId : partnerId;
        long femaleId = me.getGender() == Gender.MALE ? partnerId : userId;

        Couple couple = coupleRepository.findByPair(maleId, femaleId).orElse(null);
        if (couple != null && couple.isConnected()) {
            throw new BusinessException(ErrorCode.ALREADY_CONNECTED);
        }

        // 상대가 이미 내 코드를 입력해 요청해둔 상태? (inviter=나, invitee=상대, REQUESTED)
        Optional<Invitation> partnersRequest = invitationRepository
                .findByInviterIdAndInviteeIdAndStatus(userId, partnerId, InvitationStatus.REQUESTED);

        if (partnersRequest.isPresent()) {
            Couple connected = (couple != null) ? couple
                    : coupleRepository.save(Couple.createWait(maleId, femaleId));
            connected.connect();
            partnersRequest.get().accept();
            coupleRepository.flush();
            cleanupOtherPending(connected.getId(), userId, partnerId);
            return CoupleConnectResponseDTO.of(connected, partner);
        }

        // 내가 먼저 요청 — WAIT 생성/유지 + 내 요청 row 보장
        Couple waiting = couple;
        if (waiting == null) {
            waiting = coupleRepository.save(Couple.createWait(maleId, femaleId));
        } else if (waiting.getStatus() == CoupleStatus.DISCONNECTED) {
            waiting.reopen();
        }
        invitationRepository.findByInviterIdAndInviteeId(partnerId, userId)
                .orElseGet(() -> invitationRepository.save(
                        Invitation.createRequest(partnerId, userId, master.getInviteCode())));
        return CoupleConnectResponseDTO.of(waiting, partner);
    }

    @Override
    @Transactional
    public CoupleConnectResponseDTO accept(Long userId, Long partnerUserId) {
        User me = findUser(userId);
        User partner = userRepository.findById(partnerUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));

        Invitation request = invitationRepository
                .findByInviterIdAndInviteeIdAndStatus(userId, partnerUserId, InvitationStatus.REQUESTED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));

        assertNotConnectedElsewhere(userId, partnerUserId);
        assertNotConnectedElsewhere(partnerUserId, userId);

        long maleId = me.getGender() == Gender.MALE ? userId : partnerUserId;
        long femaleId = me.getGender() == Gender.MALE ? partnerUserId : userId;

        Couple couple = coupleRepository.findByPair(maleId, femaleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));
        if (couple.isConnected()) {
            throw new BusinessException(ErrorCode.ALREADY_CONNECTED);
        }

        couple.connect();
        request.accept();
        coupleRepository.flush();
        cleanupOtherPending(couple.getId(), userId, partnerUserId);
        return CoupleConnectResponseDTO.of(couple, partner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoupleStatusResponseDTO> getStatuses(Long userId) {
        return coupleRepository.findActiveByUserId(userId).stream()
                .map(couple -> {
                    Long partnerId = couple.partnerOf(userId);
                    User partner = findUser(partnerId);
                    return CoupleStatusResponseDTO.of(
                            couple.getId(), resolveView(couple, userId, partnerId), partner);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoupleMeResponseDTO getMe(Long userId) {
        Couple couple = coupleRepository.findConnectedByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPLE_NOT_FOUND));
        User me = findUser(userId);
        User partner = findUser(couple.partnerOf(userId));
        OnboardingStep step = onboardingStepResolver.resolve(me);
        // TODO(goal): jointRiskProfileType — 공동 목표 온보딩 완료 후 goal 에서 채운다
        return CoupleMeResponseDTO.of(couple, me, partner, step, null);
    }

    @Override
    @Transactional
    public CoupleDisconnectResponseDTO disconnect(Long userId) {
        List<Couple> active = coupleRepository.findActiveByUserId(userId);
        if (active.isEmpty()) {
            throw new BusinessException(ErrorCode.COUPLE_NOT_FOUND);
        }
        Couple couple = active.stream()
                .filter(Couple::isConnected)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CONNECTED));
        couple.disconnect();
        return CoupleDisconnectResponseDTO.of(couple);
    }

    private CoupleStatus resolveView(Couple couple, Long userId, Long partnerId) {
        if (couple.isConnected()) {
            return CoupleStatus.CONNECTED;
        }
        boolean partnerRequestedMe = invitationRepository
                .existsByInviterIdAndInviteeIdAndStatus(userId, partnerId, InvitationStatus.REQUESTED);
        return partnerRequestedMe ? CoupleStatus.REQUESTED : CoupleStatus.WAIT;
    }

    private void assertNotConnectedElsewhere(Long userId, Long partnerId) {
        if (coupleRepository.existsConnectedWithOther(userId, partnerId)) {
            throw new BusinessException(ErrorCode.ALREADY_CONNECTED);
        }
    }

    private void cleanupOtherPending(Long keepCoupleId, Long a, Long b) {
        coupleRepository.deleteOtherPendingByUsers(a, b, keepCoupleId);
        invitationRepository.deleteOtherPendingRequests(a, b);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
