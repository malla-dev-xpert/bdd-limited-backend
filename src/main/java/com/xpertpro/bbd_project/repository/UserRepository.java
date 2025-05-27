package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository @Transactional
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    UserEntity findByUsername(String userName);
    Page<UserEntity> findByStatusEnum(StatusEnum status, Pageable pageable);
    @Query("SELECT u FROM UserEntity u WHERE " +
            "u.statusEnum = :statusEnum AND " +
            "(LOWER(u.firstName) LIKE LOWER(:query) OR " +
            "LOWER(u.lastName) LIKE LOWER(:query))")
    Page<UserEntity> findByStatusEnumAndSearchQuery(
            @Param("status") StatusEnum status,
            @Param("query") String query,
            Pageable pageable
    );

    boolean existsByEmail(String email);
}
