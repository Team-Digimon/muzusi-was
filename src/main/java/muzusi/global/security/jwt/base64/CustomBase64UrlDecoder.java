package muzusi.global.security.jwt.base64;

import io.jsonwebtoken.io.Decoder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class CustomBase64UrlDecoder implements Decoder<InputStream, InputStream> {

    @Override
    public InputStream decode(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            byte[] decodedBytes = Base64.getUrlDecoder().decode(bytes);
            return new ByteArrayInputStream(decodedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decoding failed", e);
        }
    }
}