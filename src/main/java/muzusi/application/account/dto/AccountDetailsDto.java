package muzusi.application.account.dto;

import muzusi.domain.account.entity.Account;

public record AccountDetailsDto(
        Long id,
        long balance,
        long reservedPrice,
        double totalRateOfReturn,
        long totalEvaluatedAmount,
        long totalProfitAmount
) {
    public static AccountDetailsDto from(Account account, AccountSummaryDto summary) {
        return new AccountDetailsDto(
                account.getId(),
                account.getBalance(),
                account.getReservedPrice(),
                summary.totalRateOfReturn(),
                summary.totalEvaluatedAmount(),
                summary.totalProfitAmount()
        );
    }
}
