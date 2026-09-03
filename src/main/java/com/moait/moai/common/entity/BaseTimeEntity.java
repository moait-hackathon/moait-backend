package com.moait.moai.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * 생성일시({@code created_at}) + 수정일시({@code updated_at}) 를 자동 관리하는 상위 클래스.
 * {@code updated_at} 컬럼이 없는 엔티티는 {@link BaseCreatedEntity} 를 사용한다.
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedEntity {

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
