package com.jayeshshinde.walletpaymentplatform.mapper;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferDTO;
import com.jayeshshinde.walletpaymentplatform.entity.Transfer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    Transfer toTransfer(TransferDTO transferDTO);

    TransferDTO toTransferDTO(Transfer transfer);
}
