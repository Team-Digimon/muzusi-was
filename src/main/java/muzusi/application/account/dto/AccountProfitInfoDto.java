package muzusi.application.account.dto;

import muzusi.domain.account.entity.AccountProfit;

import java.time.LocalDate;

public record AccountProfitInfoDto(
        long totalBalance,
        LocalDate createdAt
) {
    public static AccountProfitInfoDto fromEntity(AccountProfit accountProfit) {
        return new AccountProfitInfoDto(accountProfit.getTotalBalance(), accountProfit.getCreatedAt());
    }
}
