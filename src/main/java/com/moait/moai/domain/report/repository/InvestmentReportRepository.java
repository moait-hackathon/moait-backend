package com.moait.moai.domain.report.repository;

import com.moait.moai.domain.report.entity.InvestmentReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentReportRepository extends JpaRepository<InvestmentReport, Long> {

    Optional<InvestmentReport> findFirstByGoalIdOrderByCreatedAtDesc(Long goalId);
}
