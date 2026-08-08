package com.jayeshshinde.walletpaymentplatform.repository;

import com.jayeshshinde.walletpaymentplatform.entity.EventTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventTransferRepository extends JpaRepository<EventTransfer, UUID> {
    @Query(value = "SELECT * " +
            "FROM event_transfer et " + // Note: Use native snake_case table name
            "WHERE NOW()>et.not_eligible_before AND (et.status = 'PENDING') " +
            "OR (et.status = 'CLAIMED' AND et.claimed_expiry < :now) " +
            "ORDER BY et.created_at ASC " +
            "LIMIT :batchSize " +      // Crucial for queue processing
            "FOR UPDATE SKIP LOCKED",  // Standard Postgres native lock
            nativeQuery = true)
    List<EventTransfer> fetchClaimTransferCompleteEvent(@Param("now") Instant now, @Param("batchSize") int batchSize);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE EventTransfer et " +
            "SET et.status = 'CLAIMED'," +
            " et.claimedExpiry=:expiry , " +
            " et.updatedAt=:updateAt " +
            " where et.id IN :ids"
    )
    void claimTransferCompleteEvent(@Param("ids") List<UUID> ids, @Param("expiry") Instant expiry, @Param("updateAt") Instant updateAt);

}
