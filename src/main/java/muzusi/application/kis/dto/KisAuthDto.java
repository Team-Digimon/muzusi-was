package muzusi.application.kis.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KisAuthDto {

    @NoArgsConstructor
    @Getter
    public static class AccessToken {
        private String value;

        @Builder
        public AccessToken(String value) {
            this.value = value;
        }
    }

    @NoArgsConstructor
    @Getter
    public static class WebSocketKey {
        private String value;

        @Builder
        public WebSocketKey(String value) {
            this.value = value;
        }
    }
}