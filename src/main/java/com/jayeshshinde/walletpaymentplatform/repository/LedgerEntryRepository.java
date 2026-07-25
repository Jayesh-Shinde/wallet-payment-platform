package com.jayeshshinde.walletpaymentplatform.repository;

import com.jayeshshinde.walletpaymentplatform.entity.LedgerEntry;
import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    @Query("SELECT SUM(CASE WHEN le.entryType = :debitType " +
            "THEN -le.amount ELSE le.amount END) " +
            "FROM LedgerEntry le WHERE le.walletId = :walletId")
    Long calculateBalanceByWalletId(@Param("walletId") UUID walletId, @Param("debitType") LedgerEntryType debitType);

    List<LedgerEntry> findByTransferId(UUID transferId);
}
