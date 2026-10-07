package com.example.loanapplication.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TotpService {
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private final SecureRandom random = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes)
                .replace("=", "").replace("+", "A").replace("/", "B");
    }

    public boolean verifyCode(String secret, String code) {
        try {
            byte[] key = secret.getBytes();
            long counter = System.currentTimeMillis() / 1000 / 30;

            for (long offset = -1; offset <= 1; offset++) {
                String expected = generateCode(key, counter + offset);
                if (expected.equals(code)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateCode(byte[] key, long counter) throws Exception {
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0f;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);

        return String.format("%06d", binary % 1_000_000);
    }

    public String provisioningUri(String email, String secret) {
        return "otpauth://totp/LoanApp:" + email +
                "?secret=" + secret +
                "&issuer=LoanApp&algorithm=SHA1&digits=6&period=30";
    }
}
