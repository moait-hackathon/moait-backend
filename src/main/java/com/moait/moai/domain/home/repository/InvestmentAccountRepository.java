package com.moait.moai.domain.home.repository;

import com.moait.moai.domain.home.entity.InvestmentAccount;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, Long> {

    List<InvestmentAccount> findAllByUserIdInAndActiveTrue(Collection<Long> userIds);
}
