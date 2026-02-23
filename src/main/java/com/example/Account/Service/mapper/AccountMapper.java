package com.example.Account.Service.mapper;

import com.example.Account.Service.model.dto.AccountDto;
import com.example.Account.Service.model.entity.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountMapper {

    public AccountDto toDto(Account entity) {
        if (entity == null) {
            return null;
        }
        return AccountDto.builder()
                .accountId(entity.getAccountId())
                .accountNumber(entity.getAccountNumber())
                .userId(entity.getUserId())
                .balance(entity.getBalance())
                .accountType(entity.getAccountType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<AccountDto> toDtoList(List<Account> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
