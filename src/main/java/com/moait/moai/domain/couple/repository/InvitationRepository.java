package com.moait.moai.domain.couple.repository;

import com.moait.moai.common.enums.InvitationStatus;
import com.moait.moai.domain.couple.entity.Invitation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    boolean existsByInviteCode(String inviteCode);

    /** 코드 소유자 조회용 마스터 row ({@code invitee_id} 가 NULL). */
    Optional<Invitation> findByInviteCodeAndInviteeIdIsNull(String inviteCode);

    /** 사용자의 초대 코드 (가입 시 발급된 마스터 row). */
    Optional<Invitation> findByInviterIdAndInviteeIdIsNull(Long inviterId);

    /** 특정 사용자가 특정 코드로 만든 요청/수락 복사 row. */
    Optional<Invitation> findByInviterIdAndInviteeId(Long inviterId, Long inviteeId);

    /** {@code inviteeId} 가 {@code inviterId} 의 코드를 입력해 대기 중인 요청. */
    Optional<Invitation> findByInviterIdAndInviteeIdAndStatus(
            Long inviterId, Long inviteeId, InvitationStatus status);

    boolean existsByInviterIdAndInviteeIdAndStatus(
            Long inviterId, Long inviteeId, InvitationStatus status);

    /** 연결 확정 직후, 두 사람에게 걸려 있던 다른 {@code REQUESTED} 요청 정리. */
    @Modifying(clearAutomatically = true)
    @Query("delete from Invitation i where i.status = com.moait.moai.common.enums.InvitationStatus.REQUESTED "
            + "and (i.inviterId in (:a, :b) or i.inviteeId in (:a, :b)) "
            + "and not ((i.inviterId = :a and i.inviteeId = :b) or (i.inviterId = :b and i.inviteeId = :a))")
    void deleteOtherPendingRequests(@Param("a") Long a, @Param("b") Long b);
}
