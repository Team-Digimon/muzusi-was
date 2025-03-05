package muzusi.infrastructure.kis.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KisWebSocketTransactionType {
    H0STCNT0("H0STCNT0");

    private final String value;

    public static KisWebSocketTransactionType of(String value) {
        for (KisWebSocketTransactionType type : KisWebSocketTransactionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}