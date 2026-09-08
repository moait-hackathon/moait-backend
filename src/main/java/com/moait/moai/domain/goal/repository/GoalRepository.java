package com.moait.moai.domain.goal.repository;

import com.moait.moai.domain.goal.entity.Goal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    /** 복수 연결은 서비스에서 거절하며 임의의 커플을 선택하지 않는다. */
    @Transactional(readOnly = true)
    @Query(value = """
            SELECT g.* FROM goal g
            JOIN couple c ON c.id = g.couple_id
            JOIN `user` male ON male.id = c.male_id
            JOIN `user` female ON female.id = c.female_id
            WHERE (c.male_id = :userId OR c.female_id = :userId)
              AND c.status = 'CONNECTED'
              AND male.is_deleted = FALSE AND female.is_deleted = FALSE
            """, nativeQuery = true)
    List<Goal> findConnectedGoalsByUserId(@Param("userId") Long userId);
}
