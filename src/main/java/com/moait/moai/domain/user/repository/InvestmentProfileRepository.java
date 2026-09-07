package com.moait.moai.domain.user.repository;

import com.moait.moai.domain.user.entity.InvestmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentProfileRepository extends JpaRepository<InvestmentProfile, Long> {

    boolean existsByUserIdAndIsLatestTrue(Long userId);

    /** 재설문 전, 해당 사용자의 기존 최신 프로필을 모두 최신 아님으로 전환. */
    @Modifying(clearAutomatically = true)
    @Query("update InvestmentProfile p set p.isLatest = false "
            + "where p.userId = :userId and p.isLatest = true")
    void markAllStale(@Param("userId") Long userId);
}
