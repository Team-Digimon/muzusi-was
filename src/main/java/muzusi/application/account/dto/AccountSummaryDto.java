package muzusi.application.account.dto;

public record AccountSummaryDto(
        double totalRateOfReturn,
        long totalEvaluatedAmount,
        long totalProfitAmount
) {
    public static AccountSummaryDto of(double totalRateOfReturn, long totalEvaluatedAmount, long totalProfitAmount) {
        return new AccountSummaryDto(totalRateOfReturn, totalEvaluatedAmount, totalProfitAmount);
    }
}
