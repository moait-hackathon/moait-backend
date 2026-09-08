package com.moait.moai.domain.report.repository;

import com.moait.moai.domain.report.entity.InvestmentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface InvestmentReportRepository extends JpaRepository<InvestmentReport, Long> {

    @Transactional(readOnly = true)
    @Query(value = """
            SELECT COUNT(*) FROM goal g
            WHERE g.id = :goalId
            """, nativeQuery = true)
    Long countGoal(@Param("goalId") Long goalId);
}
