package com.example.Account.Service.model.dto;

import com.example.Account.Service.model.entity.Account.AccountStatus;
import com.example.Account.Service.model.entity.Account.AccountType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountRequest {

    private AccountType accountType;
    private AccountStatus status;
}
