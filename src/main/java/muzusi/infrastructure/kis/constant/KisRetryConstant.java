package muzusi.infrastructure.kis.constant;

public final class KisRetryConstant {
    public static final int MAX_ATTEMPTS = 3;
    public static final long INITIAL_DELAY_MS = 1000L;
    public static final double MULTIPLIER = 2.0;
    public static final long MAX_DELAY_MS = 5000L;

    private KisRetryConstant() {
    }
}
