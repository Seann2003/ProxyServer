package com.example.mindarkproxy.common.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EncryptUtils {
    public String encodeToken(String token, String secretKey) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] encryptedBytes = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
        String encodedToken = Base64.getEncoder().encodeToString(encryptedBytes);

        return encodedToken;
    }

    public String decodeToken(String encodedToken, String secretKey) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] encryptedBytes = Base64.getDecoder().decode(encodedToken);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decodedToken = new String(decryptedBytes, StandardCharsets.UTF_8);

        return decodedToken;
    }
}

