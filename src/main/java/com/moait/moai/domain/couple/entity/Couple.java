package com.moait.moai.domain.couple.entity;

import com.moait.moai.common.entity.BaseCreatedEntity;
import com.moait.moai.common.enums.CoupleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커플 연결. 남/녀 각 1명이며 {@code male_id}/{@code female_id} 는 성별로 배정된다.
 *
 * <p>한쪽이 상대 코드를 입력하면 {@code WAIT} 로 생성되고, 다른 쪽이 코드 입력 또는 수락하면
 * {@code CONNECTED} 로 전환된다. 해제는 row 를 지우지 않고 {@code DISCONNECTED} 로 남긴다.
 * {@code UNIQUE(male_id, female_id)} 이므로 한 쌍당 row 는 최대 1개.
 */
@Entity
@Table(name = "couple")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Couple extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "male_id", nullable = false)
    private Long maleId;

    @Column(name = "female_id", nullable = false)
    private Long femaleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private CoupleStatus status;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    private Couple(Long maleId, Long femaleId) {
        this.maleId = maleId;
        this.femaleId = femaleId;
        this.status = CoupleStatus.WAIT;
    }

    /** 한쪽이 상대 코드를 입력해 연결 요청 — {@code WAIT} 로 생성. */
    public static Couple createWait(Long maleId, Long femaleId) {
        return new Couple(maleId, femaleId);
    }

    /** {@code WAIT} → {@code CONNECTED}. */
    public void connect() {
        this.status = CoupleStatus.CONNECTED;
        this.connectedAt = LocalDateTime.now();
    }

    /** {@code CONNECTED} → {@code DISCONNECTED} (이력 보존). */
    public void disconnect() {
        this.status = CoupleStatus.DISCONNECTED;
    }

    /** 해제된 커플이 다시 연결을 시작 — {@code DISCONNECTED} → {@code WAIT}. */
    public void reopen() {
        this.status = CoupleStatus.WAIT;
        this.connectedAt = null;
    }

    public boolean isConnected() {
        return status == CoupleStatus.CONNECTED;
    }

    public boolean involves(Long userId) {
        return maleId.equals(userId) || femaleId.equals(userId);
    }

    /** 두 구성원 중 {@code userId} 가 아닌 쪽. */
    public Long partnerOf(Long userId) {
        return maleId.equals(userId) ? femaleId : maleId;
    }
}
