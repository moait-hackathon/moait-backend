package com.moait.moai.domain.couple.dto;

import com.moait.moai.common.enums.Gender;
import com.moait.moai.domain.user.entity.User;

/** 커플 구성원(나 / 파트너) 요약. */
public record CoupleMemberDTO(Long userId, String name, Gender gender) {

    public static CoupleMemberDTO of(User user) {
        return new CoupleMemberDTO(user.getId(), user.getName(), user.getGender());
    }
}
