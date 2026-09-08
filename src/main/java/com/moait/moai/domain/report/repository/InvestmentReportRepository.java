package com.moait.moai.domain.report.repository;

import com.moait.moai.domain.report.entity.InvestmentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentReportRepository extends JpaRepository<InvestmentReport, Long> {
}
