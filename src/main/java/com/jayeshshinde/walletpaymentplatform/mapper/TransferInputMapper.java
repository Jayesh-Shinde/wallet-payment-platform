package com.jayeshshinde.walletpaymentplatform.mapper;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferInputMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Transfer toTransfer(TransferInputDTO transferInputDTO);

}
