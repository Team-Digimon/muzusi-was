package muzusi.application.auth.dto;

public record LoginDto(
        TokenDto tokenDto,
        boolean isRegistered
) {
    public static LoginDto of(TokenDto tokenDto, boolean isRegistered) {
        return new LoginDto(tokenDto, isRegistered);
    }
}
