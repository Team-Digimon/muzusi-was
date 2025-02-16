package muzusi.application.account.dto;

import muzusi.domain.account.entity.Account;

import java.time.LocalDateTime;

public record AccountInfoDto(
        Long id,
        Long balance,
        Long reservedPrice,
        LocalDateTime createdAt
) {
    public static AccountInfoDto fromEntity(Account account) {
        return new AccountInfoDto(
                account.getId(),
                account.getBalance(),
                account.getReservedPrice(),
                account.getCreatedAt()
        );
    }
}