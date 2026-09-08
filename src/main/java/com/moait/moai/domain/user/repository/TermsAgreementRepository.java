package com.moait.moai.domain.user.repository;

import com.moait.moai.domain.user.entity.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {
}
