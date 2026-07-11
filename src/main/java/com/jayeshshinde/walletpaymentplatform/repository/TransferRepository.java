package com.jayeshshinde.walletpaymentplatform.repository;

import com.jayeshshinde.walletpaymentplatform.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
}
