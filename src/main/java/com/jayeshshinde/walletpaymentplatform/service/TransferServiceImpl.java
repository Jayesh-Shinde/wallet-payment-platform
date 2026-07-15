package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.entity.LedgerEntry;
import com.jayeshshinde.walletpaymentplatform.entity.Transfer;
import com.jayeshshinde.walletpaymentplatform.entity.Wallet;
import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import com.jayeshshinde.walletpaymentplatform.enums.WalletStatus;
import com.jayeshshinde.walletpaymentplatform.mapper.TransferInputMapper;
import com.jayeshshinde.walletpaymentplatform.mapper.TransferOutputMapper;
import com.jayeshshinde.walletpaymentplatform.repository.LedgerEntryRepository;
import com.jayeshshinde.walletpaymentplatform.repository.TransferRepository;
import com.jayeshshinde.walletpaymentplatform.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransferInputMapper transferMapper;
    private final TransferOutputMapper transferOutputMapper;

    @Override
    @Transactional
    public TransferOutputDTO createTransfer(TransferInputDTO transferInputDTO) {
        if (transferInputDTO.getAmount() < 1) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        if (transferInputDTO.getFromWalletId().equals(transferInputDTO.getToWalletId())) {
            throw new IllegalArgumentException("From Wallet Id must be different from to Wallet Id.");
        }

        List<UUID> walletIds = new ArrayList<>();
        walletIds.add(transferInputDTO.getFromWalletId());
        walletIds.add(transferInputDTO.getToWalletId());
        walletIds.sort(Comparator.naturalOrder());
        List<Wallet> wallets = new ArrayList<>();
        for (UUID walletId : walletIds) {
            wallets.add(walletRepository.findWithLockById(walletId)
                    .orElseThrow(() -> new NoSuchElementException("wallet not found.")));
        }
        Transfer transfer = transferMapper.toTransfer(transferInputDTO);

        Long fromWalletBalance = ledgerEntryRepository.calculateBalanceByWalletId(transferInputDTO.getFromWalletId(), LedgerEntryType.DEBIT);
        boolean sufficientBalance = fromWalletBalance < transferInputDTO.getAmount();
        if (sufficientBalance) {
            transfer.applyStatusReason("insufficient_balance");
        }
        boolean walletStatusValidation = validateWalletStatusForTransfer(wallets.getFirst(), wallets.getLast());
        if (walletStatusValidation) {
            transfer.applyStatusReason("wallet_status_validation");
        }

        Transfer saveTransfer = transferRepository.save(transfer);
        if (!walletStatusValidation || sufficientBalance) {
            saveTransfer.failedTransfer();
            return transferOutputMapper.toTransferOutputDTO(saveTransfer);
        }

        saveTransfer.initiateTransfer();
        LedgerEntry debit = new LedgerEntry(saveTransfer.getId(),
                LedgerEntryType.DEBIT,
                saveTransfer.getAmount(),
                saveTransfer.getFromWalletId());
        ledgerEntryRepository.save(debit);
        LedgerEntry credit = new LedgerEntry(saveTransfer.getId(),
                LedgerEntryType.CREDIT,
                saveTransfer.getAmount(),
                saveTransfer.getToWalletId());
        ledgerEntryRepository.save(credit);
        saveTransfer.completeTransfer();
        return transferOutputMapper.toTransferOutputDTO(saveTransfer);
    }

    private static boolean validateWalletStatusForTransfer(Wallet wallet1, Wallet wallet2) {
        return !(wallet1.getStatus() == WalletStatus.DEACTIVATED ||
                wallet2.getStatus() == WalletStatus.DEACTIVATED ||
                wallet2.getStatus() == WalletStatus.PENDING_VERIFICATION ||
                wallet1.getStatus() == WalletStatus.PENDING_VERIFICATION);
    }
}
