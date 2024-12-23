package muzusi.application.auth.dto;

import muzusi.domain.user.entity.User;

public record UserStatusDto(
        User user,
        boolean isRegistered
) {
    public static UserStatusDto of(User user, boolean isRegistered) {
        return new UserStatusDto(user, isRegistered);
    }
}
