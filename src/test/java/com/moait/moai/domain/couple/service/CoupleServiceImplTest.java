package com.moait.moai.domain.couple.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moait.moai.common.enums.CoupleStatus;
import com.moait.moai.common.enums.Gender;
import com.moait.moai.common.enums.InvitationStatus;
import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.couple.dto.CoupleConnectResponseDTO;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.entity.Invitation;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.couple.repository.InvitationRepository;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.UserRepository;
import com.moait.moai.domain.user.service.OnboardingStepResolver;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoupleServiceImplTest {

    private static final long ME = 1L;
    private static final long PARTNER = 2L;
    private static final String CODE = "8F3K2Q";

    @Mock
    private CoupleRepository coupleRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OnboardingStepResolver onboardingStepResolver;

    @InjectMocks
    private CoupleServiceImpl service;

    private User me;
    private User partner;

    @BeforeEach
    void setUp() {
        me = mock(User.class);
        partner = mock(User.class);
        lenient().when(me.getId()).thenReturn(ME);
        lenient().when(me.getName()).thenReturn("나");
        lenient().when(me.getGender()).thenReturn(Gender.MALE);
        lenient().when(partner.getId()).thenReturn(PARTNER);
        lenient().when(partner.getName()).thenReturn("상대");
        lenient().when(partner.getGender()).thenReturn(Gender.FEMALE);
    }

    private void mockCodeOwnedByPartner() {
        when(userRepository.findById(ME)).thenReturn(Optional.of(me));
        when(invitationRepository.findByInviteCodeAndInviteeIdIsNull(CODE))
                .thenReturn(Optional.of(Invitation.createMaster(PARTNER, CODE)));
        lenient().when(userRepository.findById(PARTNER)).thenReturn(Optional.of(partner));
    }

    @Test
    @DisplayName("본인 코드 입력 → CANNOT_CONNECT_SELF")
    void connectSelf() {
        when(userRepository.findById(ME)).thenReturn(Optional.of(me));
        when(invitationRepository.findByInviteCodeAndInviteeIdIsNull(CODE))
                .thenReturn(Optional.of(Invitation.createMaster(ME, CODE)));

        assertThatThrownBy(() -> service.connect(ME, CODE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CANNOT_CONNECT_SELF);
    }

    @Test
    @DisplayName("성별이 같으면 → SAME_GENDER")
    void connectSameGender() {
        mockCodeOwnedByPartner();
        when(partner.getGender()).thenReturn(Gender.MALE);

        assertThatThrownBy(() -> service.connect(ME, CODE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SAME_GENDER);
    }

    @Test
    @DisplayName("이미 다른 사람과 연결됨 → ALREADY_CONNECTED")
    void connectAlreadyConnected() {
        mockCodeOwnedByPartner();
        when(coupleRepository.existsConnectedWithOther(ME, PARTNER)).thenReturn(true);

        assertThatThrownBy(() -> service.connect(ME, CODE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALREADY_CONNECTED);
    }

    @Test
    @DisplayName("상대가 아직 안 함 → WAIT 커플 생성 + 요청 row 저장")
    void connectCreatesWait() {
        mockCodeOwnedByPartner();
        when(coupleRepository.findByPair(ME, PARTNER)).thenReturn(Optional.empty());
        when(invitationRepository.findByInviterIdAndInviteeIdAndStatus(ME, PARTNER, InvitationStatus.REQUESTED))
                .thenReturn(Optional.empty());
        when(coupleRepository.save(any(Couple.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invitationRepository.findByInviterIdAndInviteeId(PARTNER, ME)).thenReturn(Optional.empty());

        CoupleConnectResponseDTO result = service.connect(ME, CODE);

        assertThat(result.status()).isEqualTo(CoupleStatus.WAIT);
        assertThat(result.partner().userId()).isEqualTo(PARTNER);
        verify(invitationRepository).save(any(Invitation.class));
        verify(coupleRepository, never()).flush();
    }

    @Test
    @DisplayName("상대가 이미 내 코드 입력해둠(REQUESTED) → 즉시 CONNECTED")
    void connectImmediately() {
        mockCodeOwnedByPartner();
        when(coupleRepository.findByPair(ME, PARTNER)).thenReturn(Optional.empty());
        Invitation partnersRequest = Invitation.createRequest(ME, PARTNER, CODE);
        when(invitationRepository.findByInviterIdAndInviteeIdAndStatus(ME, PARTNER, InvitationStatus.REQUESTED))
                .thenReturn(Optional.of(partnersRequest));
        when(coupleRepository.save(any(Couple.class))).thenAnswer(inv -> inv.getArgument(0));

        CoupleConnectResponseDTO result = service.connect(ME, CODE);

        assertThat(result.status()).isEqualTo(CoupleStatus.CONNECTED);
        assertThat(result.connectedAt()).isNotNull();
        assertThat(partnersRequest.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(invitationRepository).deleteOtherPendingRequests(ME, PARTNER);
    }

    @Test
    @DisplayName("대기 중 요청 없음 → REQUEST_NOT_FOUND")
    void acceptNoRequest() {
        when(userRepository.findById(ME)).thenReturn(Optional.of(me));
        when(userRepository.findById(PARTNER)).thenReturn(Optional.of(partner));
        when(invitationRepository.findByInviterIdAndInviteeIdAndStatus(ME, PARTNER, InvitationStatus.REQUESTED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accept(ME, PARTNER))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("요청 수락 → CONNECTED + invitation ACCEPTED")
    void acceptHappy() {
        when(userRepository.findById(ME)).thenReturn(Optional.of(me));
        when(userRepository.findById(PARTNER)).thenReturn(Optional.of(partner));
        Invitation request = Invitation.createRequest(ME, PARTNER, CODE);
        when(invitationRepository.findByInviterIdAndInviteeIdAndStatus(ME, PARTNER, InvitationStatus.REQUESTED))
                .thenReturn(Optional.of(request));
        Couple couple = Couple.createWait(ME, PARTNER);
        when(coupleRepository.findByPair(ME, PARTNER)).thenReturn(Optional.of(couple));

        CoupleConnectResponseDTO result = service.accept(ME, PARTNER);

        assertThat(result.status()).isEqualTo(CoupleStatus.CONNECTED);
        assertThat(request.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(couple.isConnected()).isTrue();
    }

    @Test
    @DisplayName("연결된 커플 없음 → disconnect 시 COUPLE_NOT_FOUND")
    void disconnectNone() {
        when(coupleRepository.findActiveByUserId(ME)).thenReturn(java.util.List.of());

        assertThatThrownBy(() -> service.disconnect(ME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPLE_NOT_FOUND);
    }

    @Test
    @DisplayName("WAIT 상태만 있고 CONNECTED 없음 → disconnect 시 NOT_CONNECTED")
    void disconnectNotConnected() {
        when(coupleRepository.findActiveByUserId(ME))
                .thenReturn(java.util.List.of(Couple.createWait(ME, PARTNER)));

        assertThatThrownBy(() -> service.disconnect(ME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CONNECTED);
    }

    @Test
    @DisplayName("getInviteCode → 코드 + 공유 URL")
    void inviteCode() {
        when(invitationRepository.findByInviterIdAndInviteeIdIsNull(ME))
                .thenReturn(Optional.of(Invitation.createMaster(ME, CODE)));

        var result = service.getInviteCode(ME);

        assertThat(result.inviteCode()).isEqualTo(CODE);
        assertThat(result.shareUrl()).isEqualTo("https://moait.app/invite/" + CODE);
    }

    @Test
    @DisplayName("getInviteCode - 마스터 row 없음 → INVITATION_NOT_FOUND")
    void inviteCodeMissing() {
        when(invitationRepository.findByInviterIdAndInviteeIdIsNull(ME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getInviteCode(ME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVITATION_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 코드 → INVITATION_NOT_FOUND")
    void connectUnknownCode() {
        when(userRepository.findById(ME)).thenReturn(Optional.of(me));
        when(invitationRepository.findByInviteCodeAndInviteeIdIsNull(CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.connect(ME, CODE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVITATION_NOT_FOUND);
    }
}
