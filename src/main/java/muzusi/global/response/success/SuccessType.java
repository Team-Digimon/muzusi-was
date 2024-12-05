package muzusi.global.response.success;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessType {

    ;

    private final HttpStatus status;
    private final String message;

    public int getStatusCode(){
        return status.value();
    }
}
