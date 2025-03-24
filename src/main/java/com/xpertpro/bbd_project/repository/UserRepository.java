package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository @Transactional
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    UserEntity findByUsername(String userName);

    @Query(value = "UPDATE users SET status_enum='DISABLE' WHERE id=:id", nativeQuery = true)
    @Modifying
    public void disableUserAccount(@Param("id") Long id);
}
