package com.moait.moai.common.enums;

/** 약관 종류. {@code MARKETING} 을 제외한 나머지는 회원가입 필수 동의. */
public enum TermsType {
    SERVICE,
    PRIVACY,
    FINANCE,
    MARKETING;

    public boolean isRequired() {
        return this != MARKETING;
    }
}
