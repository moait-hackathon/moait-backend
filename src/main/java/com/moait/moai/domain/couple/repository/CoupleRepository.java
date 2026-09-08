package com.moait.moai.domain.couple.repository;

import com.moait.moai.domain.couple.entity.Couple;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    /** 내가 관여된 커플 중 {@code DISCONNECTED} 가 아닌 것 (동시에 여러 건 가능). */
    @Query("select c from Couple c "
            + "where (c.maleId = :userId or c.femaleId = :userId) "
            + "and c.status <> com.moait.moai.common.enums.CoupleStatus.DISCONNECTED")
    List<Couple> findActiveByUserId(@Param("userId") Long userId);

    /** 두 사람 쌍의 커플 row ({@code DISCONNECTED} 포함, {@code UNIQUE} 로 최대 1개). */
    @Query("select c from Couple c "
            + "where (c.maleId = :maleId and c.femaleId = :femaleId)")
    Optional<Couple> findByPair(@Param("maleId") Long maleId, @Param("femaleId") Long femaleId);

    /** 내가 {@code CONNECTED} 상태인 커플. */
    @Query("select c from Couple c "
            + "where (c.maleId = :userId or c.femaleId = :userId) "
            + "and c.status = com.moait.moai.common.enums.CoupleStatus.CONNECTED")
    Optional<Couple> findConnectedByUserId(@Param("userId") Long userId);

    /** {@code userId} 가 {@code partnerId} 가 아닌 다른 사람과 이미 {@code CONNECTED} 인지. */
    @Query("select count(c) > 0 from Couple c "
            + "where (c.maleId = :userId or c.femaleId = :userId) "
            + "and c.status = com.moait.moai.common.enums.CoupleStatus.CONNECTED "
            + "and c.maleId <> :partnerId and c.femaleId <> :partnerId")
    boolean existsConnectedWithOther(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

    /** 연결 확정 직후, 두 사람에게 걸려 있던 다른 {@code WAIT} 요청 정리 (성사 불가). */
    @Modifying(clearAutomatically = true)
    @Query("delete from Couple c where c.status = com.moait.moai.common.enums.CoupleStatus.WAIT "
            + "and (c.maleId in (:a, :b) or c.femaleId in (:a, :b)) "
            + "and c.id <> :keepId")
    void deleteOtherPendingByUsers(@Param("a") Long a, @Param("b") Long b, @Param("keepId") Long keepId);
}
