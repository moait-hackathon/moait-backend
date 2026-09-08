package com.moait.moai.domain.couple.entity;

import com.moait.moai.common.entity.BaseCreatedEntity;
import com.moait.moai.common.enums.InvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커플 초대. 회원가입 시 <b>마스터 row</b>({@code invitee_id = null}, {@code CREATED}) 1개가 생성되고,
 * 다른 사용자가 그 코드를 입력하면 <b>요청자별 복사 row</b>({@code REQUESTED} → {@code ACCEPTED}) 가 추가된다.
 */
@Entity
@Table(name = "invitation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(name = "invitee_id")
    private Long inviteeId;

    @Column(name = "invite_code", nullable = false, length = 30)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvitationStatus status;

    private Invitation(Long inviterId, Long inviteeId, String inviteCode, InvitationStatus status) {
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
        this.inviteCode = inviteCode;
        this.status = status;
    }

    /** 회원가입 시 발급되는 마스터 row. */
    public static Invitation createMaster(Long inviterId, String inviteCode) {
        return new Invitation(inviterId, null, inviteCode, InvitationStatus.CREATED);
    }

    /**
     * 요청자별 복사 row. {@code inviteeId} 가 {@code inviterId} 의 코드를 입력해 연결을 요청한 상태.
     * (마스터 row 의 {@code inviteCode} 를 그대로 복사)
     */
    public static Invitation createRequest(Long inviterId, Long inviteeId, String inviteCode) {
        return new Invitation(inviterId, inviteeId, inviteCode, InvitationStatus.REQUESTED);
    }

    /** {@code REQUESTED} → {@code ACCEPTED} (연결 확정). */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
    }
}
