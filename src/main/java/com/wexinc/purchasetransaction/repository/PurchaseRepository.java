package com.wexinc.purchasetransaction.repository;

import com.wexinc.purchasetransaction.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Purchase findByIdempotencyKey(String idempotencyKey);
}
