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
            JOIN couple c ON c.id = g.couple_id
            WHERE g.id = :goalId AND c.status = 'CONNECTED'
              AND (c.male_id = :userId OR c.female_id = :userId)
            """, nativeQuery = true)
    Long countAccessibleGoal(@Param("goalId") Long goalId, @Param("userId") Long userId);
}
