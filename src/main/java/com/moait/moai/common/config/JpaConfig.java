package com.moait.moai.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 공통 설정. {@code @CreatedDate} / {@code @LastModifiedDate} 자동 관리 활성화.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
