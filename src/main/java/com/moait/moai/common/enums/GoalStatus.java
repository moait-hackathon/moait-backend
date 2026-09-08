package com.moait.moai.common.enums;

/**
 * 공동 목표 상태.
 *
 * <ul>
 *   <li>{@code ACTIVE} — 온보딩 제출 완료, 진행 중</li>
 *   <li>{@code ACHIEVED} — 목표 금액 도달 ({@code current-amount} 갱신 시 자동 전환)</li>
 *   <li>{@code CANCELLED} — 취소됨 (soft delete, 이력 보존)</li>
 * </ul>
 *
 * <p>DRAFT 는 두지 않는다 — {@code POST /goals} 가 직접 INSERT 하며, 커플당 1개는
 * {@code UNIQUE(couple_id)} 로 보장한다.
 */
public enum GoalStatus {
    ACTIVE,
    ACHIEVED,
    CANCELLED
}
