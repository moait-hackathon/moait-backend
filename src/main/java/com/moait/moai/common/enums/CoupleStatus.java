package com.moait.moai.common.enums;

/**
 * 커플 연결 상태.
 *
 * <ul>
 *   <li>{@code WAIT} — 한쪽이 상대 코드를 입력해 대기 중 ({@code couple.status} 에 저장)</li>
 *   <li>{@code REQUESTED} — <b>조회 전용</b>. "상대가 내 코드를 입력함 → 내 수락 대기" 를 뜻하며
 *       DB 에는 저장하지 않는다 ({@code GET /couples/status} 응답에서만 계산되어 나온다)</li>
 *   <li>{@code CONNECTED} — 연결 완료 ({@code couple.status})</li>
 *   <li>{@code DISCONNECTED} — 해제됨, 이력 보존 ({@code couple.status})</li>
 * </ul>
 */
public enum CoupleStatus {
    WAIT,
    REQUESTED,
    CONNECTED,
    DISCONNECTED
}
