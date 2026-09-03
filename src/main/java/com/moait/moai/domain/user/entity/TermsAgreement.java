package com.moait.moai.domain.user.entity;

import com.moait.moai.common.entity.BaseCreatedEntity;
import com.moait.moai.common.enums.TermsType;
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

@Entity
@Table(name = "terms_agreement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 50)
    private TermsType termsType;

    @Column(name = "agreed")
    private Boolean agreed;

    private TermsAgreement(Long userId, TermsType termsType, Boolean agreed) {
        this.userId = userId;
        this.termsType = termsType;
        this.agreed = agreed;
    }

    public static TermsAgreement of(Long userId, TermsType termsType, Boolean agreed) {
        return new TermsAgreement(userId, termsType, agreed);
    }
}
