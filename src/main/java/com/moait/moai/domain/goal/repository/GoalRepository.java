package com.moait.moai.domain.goal.repository;

import com.moait.moai.domain.goal.entity.Goal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    Optional<Goal> findByCoupleId(Long coupleId);

    boolean existsByCoupleId(Long coupleId);
}
