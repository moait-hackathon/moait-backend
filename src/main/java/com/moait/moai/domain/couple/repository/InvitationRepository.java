package com.moait.moai.domain.couple.repository;

import com.moait.moai.domain.couple.entity.Invitation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    boolean existsByInviteCode(String inviteCode);

    /** 코드 소유자 조회용 마스터 row ({@code invitee_id} 가 NULL). */
    Optional<Invitation> findByInviteCodeAndInviteeIdIsNull(String inviteCode);

    /** 특정 사용자가 특정 코드로 만든 요청/수락 복사 row. */
    Optional<Invitation> findByInviterIdAndInviteeId(Long inviterId, Long inviteeId);
}
