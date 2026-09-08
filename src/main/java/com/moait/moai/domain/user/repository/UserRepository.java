package com.moait.moai.domain.user.repository;

import com.moait.moai.common.enums.Provider;
import com.moait.moai.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
