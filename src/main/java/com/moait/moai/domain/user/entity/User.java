package com.moait.moai.domain.user.entity;

import com.moait.moai.common.entity.BaseCreatedEntity;
import com.moait.moai.common.enums.Gender;
import com.moait.moai.common.enums.Provider;
import com.moait.moai.common.enums.Role;
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
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 10)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "connected_id")
    private String connectedId;

    @Column(name = "asset")
    private Long asset;

    @Column(name = "income")
    private Long income;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    private User(String name, String phone, String password, Role role, Gender gender,
                 Provider provider, String providerId) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.gender = gender;
        this.provider = provider;
        this.providerId = providerId;
        this.isDeleted = false;
    }

    /** 일반(LOCAL) 회원가입. */
    public static User createLocal(String name, String phone, String encodedPassword, Gender gender) {
        return new User(name, phone, encodedPassword, Role.USER, gender, Provider.LOCAL, null);
    }

    /** 소셜 회원가입. */
    public static User createSocial(String name, Gender gender, Provider provider, String providerId,
                                    String encodedPassword) {
        return new User(name, null, encodedPassword, Role.USER, gender, provider, providerId);
    }

    /** 온보딩 - 기본 재무정보 입력. */
    public void updateFinancialInfo(Long income, Long asset) {
        this.income = income;
        this.asset = asset;
    }

    public boolean hasFinancialInfo() {
        return income != null && asset != null;
    }
}
