package muzusi.application.account.dto;

import muzusi.domain.account.entity.Account;

import java.time.LocalDateTime;

public record AccountDetailsDto(
        Long id,
        long balance,
        long reservedPrice,
        LocalDateTime createdAt,
        double totalRateOfReturn,
        long totalEvaluatedAmount,
        long totalProfitAmount
) {
    public static AccountDetailsDto from(Account account, AccountSummaryDto summary) {
        return new AccountDetailsDto(
                account.getId(),
                account.getBalance(),
                account.getReservedPrice(),
                account.getCreatedAt(),
                summary.totalRateOfReturn(),
                summary.totalEvaluatedAmount(),
                summary.totalProfitAmount()
        );
    }
}
