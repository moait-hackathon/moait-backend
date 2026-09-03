package com.moait.moai.domain.auth.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.common.enums.TermsType;
import com.moait.moai.common.security.JwtTokenProvider;
import com.moait.moai.domain.auth.dto.AuthTokenResponseDTO;
import com.moait.moai.domain.auth.dto.LoginRequestDTO;
import com.moait.moai.domain.auth.dto.SignupRequestDTO;
import com.moait.moai.domain.auth.dto.SignupRequestDTO.AgreementItem;
import com.moait.moai.domain.couple.entity.Invitation;
import com.moait.moai.domain.couple.repository.InvitationRepository;
import com.moait.moai.domain.user.entity.TermsAgreement;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.TermsAgreementRepository;
import com.moait.moai.domain.user.repository.UserRepository;
import com.moait.moai.domain.user.service.OnboardingStepResolver;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int INVITE_CODE_LENGTH = 6;
    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final UserRepository userRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OnboardingStepResolver onboardingStepResolver;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AuthTokenResponseDTO signup(SignupRequestDTO request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }
        validateRequiredTermsAgreed(request.agreements());

        User user = userRepository.save(User.createLocal(
                request.name(),
                request.phone(),
                passwordEncoder.encode(request.password()),
                request.gender()));

        saveAgreements(user.getId(), request.agreements());
        invitationRepository.save(Invitation.createMaster(user.getId(), generateUniqueInviteCode()));

        return issueTokens(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokenResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user);
    }

    private AuthTokenResponseDTO issueTokens(User user) {
        return AuthTokenResponseDTO.of(
                user.getId(),
                jwtTokenProvider.createAccessToken(user.getId()),
                onboardingStepResolver.resolve(user));
    }

    private void validateRequiredTermsAgreed(List<AgreementItem> agreements) {
        Set<TermsType> agreed = EnumSet.noneOf(TermsType.class);
        for (AgreementItem item : agreements) {
            if (Boolean.TRUE.equals(item.agreed())) {
                agreed.add(item.termsType());
            }
        }
        boolean allRequiredAgreed = Arrays.stream(TermsType.values())
                .filter(TermsType::isRequired)
                .allMatch(agreed::contains);
        if (!allRequiredAgreed) {
            throw new BusinessException(ErrorCode.TERMS_REQUIRED_NOT_AGREED);
        }
    }

    private void saveAgreements(Long userId, List<AgreementItem> agreements) {
        List<TermsAgreement> entities = agreements.stream()
                .map(item -> TermsAgreement.of(userId, item.termsType(), item.agreed()))
                .toList();
        termsAgreementRepository.saveAll(entities);
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = randomCode();
        } while (invitationRepository.existsByInviteCode(code));
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(secureRandom.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
