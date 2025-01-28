package muzusi.application.account.dto;

import muzusi.domain.account.entity.Account;

public record AccountInfoDto(
        Long id,
        Long balance,
        Long reservedPrice
) {
    public static AccountInfoDto fromEntity(Account account) {
        return new AccountInfoDto(account.getId(), account.getBalance(), account.getReservedPrice());
    }
}