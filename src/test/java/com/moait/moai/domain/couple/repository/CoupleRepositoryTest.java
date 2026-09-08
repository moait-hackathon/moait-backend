package com.moait.moai.domain.couple.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.moait.moai.common.enums.Gender;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
class CoupleRepositoryTest {

    @Autowired
    private CoupleRepository repository;

    @Autowired
    private TestEntityManager em;

    private Long persistUser(String name, Gender gender) {
        User user = User.createLocal(name, null, "pw", gender);
        em.persist(user);
        return user.getId();
    }

    @Test
    @DisplayName("findActiveByUserId 는 DISCONNECTED 를 제외하고 내가 관여된 커플을 반환한다")
    void findActive() {
        Long me = persistUser("나", Gender.MALE);
        Long a = persistUser("A", Gender.FEMALE);
        Long b = persistUser("B", Gender.FEMALE);

        em.persist(Couple.createWait(me, a));
        Couple disconnected = Couple.createWait(me, b);
        disconnected.connect();
        disconnected.disconnect();
        em.persist(disconnected);
        em.flush();
        em.clear();

        assertThat(repository.findActiveByUserId(me)).hasSize(1);
    }

    @Test
    @DisplayName("findConnectedByUserId 는 CONNECTED 커플만 반환한다")
    void findConnected() {
        Long male = persistUser("남", Gender.MALE);
        Long female = persistUser("여", Gender.FEMALE);
        Couple couple = Couple.createWait(male, female);
        couple.connect();
        em.persist(couple);
        em.flush();
        em.clear();

        assertThat(repository.findConnectedByUserId(male)).isPresent();
        assertThat(repository.findConnectedByUserId(female)).isPresent();
    }

    @Test
    @DisplayName("existsConnectedWithOther 는 지정 상대가 아닌 다른 사람과 CONNECTED 면 true")
    void existsConnectedWithOther() {
        Long me = persistUser("나", Gender.MALE);
        Long other = persistUser("기존파트너", Gender.FEMALE);
        Long newbie = persistUser("새상대", Gender.FEMALE);
        Couple couple = Couple.createWait(me, other);
        couple.connect();
        em.persist(couple);
        em.flush();
        em.clear();

        assertThat(repository.existsConnectedWithOther(me, newbie)).isTrue();
        assertThat(repository.existsConnectedWithOther(me, other)).isFalse();
    }

    @Test
    @DisplayName("findByPair 는 성별로 배정된 male/female 순서로 조회한다")
    void findByPair() {
        Long male = persistUser("남", Gender.MALE);
        Long female = persistUser("여", Gender.FEMALE);
        em.persist(Couple.createWait(male, female));
        em.flush();
        em.clear();

        assertThat(repository.findByPair(male, female)).isPresent();
        assertThat(repository.findByPair(female, male)).isEmpty();
    }
}
