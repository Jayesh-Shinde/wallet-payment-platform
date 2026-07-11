package com.jayeshshinde.walletpaymentplatform.repository;

import com.jayeshshinde.walletpaymentplatform.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
}
