package muzusi.application.auth.dto;

import muzusi.domain.user.entity.User;

public record LoginDto(
        User user,
        boolean isRegistered
) {
    public static LoginDto of(User user, boolean isRegistered) {
        return new LoginDto(user, isRegistered);
    }
}
