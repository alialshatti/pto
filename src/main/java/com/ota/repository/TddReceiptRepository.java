package com.ota.repository;

import com.ota.entity.TddReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TddReceiptRepository extends JpaRepository<TddReceiptEntity, UUID> {

    @Modifying
    @Query("UPDATE TddReceiptEntity r SET r.status = :status WHERE r.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") TddReceiptEntity.ReceiptStatus status);
}
