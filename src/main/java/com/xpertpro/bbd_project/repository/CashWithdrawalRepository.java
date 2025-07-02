package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.CashWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashWithdrawalRepository extends JpaRepository<CashWithdrawal, Long> {
}
