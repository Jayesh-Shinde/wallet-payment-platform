package com.jayeshshinde.walletpaymentplatform.mapper;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferOutputMapper {
    TransferOutputDTO toTransferOutputDTO(Transfer transfer);
}
