package com.moait.moai.domain.user.repository;

import com.moait.moai.domain.user.entity.InvestmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentProfileRepository extends JpaRepository<InvestmentProfile, Long> {

    boolean existsByUserIdAndIsLatestTrue(Long userId);
}
