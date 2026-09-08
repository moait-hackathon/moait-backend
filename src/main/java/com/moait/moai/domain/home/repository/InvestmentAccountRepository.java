package com.moait.moai.domain.home.repository;

import com.moait.moai.domain.home.entity.HomeInvestmentAccount;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAccountRepository extends JpaRepository<HomeInvestmentAccount, Long> {

    List<HomeInvestmentAccount> findAllByUserIdInAndActiveTrue(Collection<Long> userIds);
}
