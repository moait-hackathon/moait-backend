package com.moait.moai.common.enums;

/**
 * 초대(커플 연결) 상태.
 *
 * <ul>
 *   <li>{@code CREATED} — 회원가입 시 발급된 마스터 row ({@code invitee_id} 가 NULL)</li>
 *   <li>{@code REQUESTED} — 상대가 코드를 입력해 연결을 요청한 복사 row</li>
 *   <li>{@code ACCEPTED} — 연결이 성사된 복사 row</li>
 * </ul>
 */
public enum InvitationStatus {
    CREATED,
    REQUESTED,
    ACCEPTED
}
