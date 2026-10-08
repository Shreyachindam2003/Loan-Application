package com.example.loanapplication.security.auth;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

@Service
public class TotpService {

    private static final String BASE32 =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final SecureRandom random = new SecureRandom();

    public String generateSecret() {

        byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        return base32(bytes);
    }

    public boolean verifyCode(String secret, String code) {

        if (secret == null || code == null || !code.matches("\\d{6}")) {
            return false;
        }

        try {

            byte[] key = decodeBase32(secret);

            long counter =
                    System.currentTimeMillis() / 1000 / 30;

            for (long offset = -1; offset <= 1; offset++) {

                if (generateCode(key, counter + offset).equals(code)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    private String generateCode(byte[] key, long counter)
            throws Exception {

        byte[] data = ByteBuffer.allocate(8)
                .putLong(counter)
                .array();

        Mac mac = Mac.getInstance("HmacSHA1");

        mac.init(new SecretKeySpec(key, "HmacSHA1"));

        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0f;

        int binary =
                ((hash[offset] & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        | (hash[offset + 3] & 0xff);

        return String.format(
                "%06d",
                binary % 1_000_000
        );
    }

    private String base32(byte[] data) {

        StringBuilder output = new StringBuilder();

        int buffer = 0;
        int bits = 0;

        for (byte b : data) {

            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;

            while (bits >= 5) {

                bits -= 5;

                output.append(
                        BASE32.charAt(
                                (buffer >> bits) & 31
                        )
                );
            }
        }

        if (bits > 0) {

            output.append(
                    BASE32.charAt(
                            (buffer << (5 - bits)) & 31
                    )
            );
        }

        return output.toString();
    }

    private byte[] decodeBase32(String value) {

        value = value
                .replace("=", "")
                .toUpperCase();

        java.io.ByteArrayOutputStream output =
                new java.io.ByteArrayOutputStream();

        int buffer = 0;
        int bits = 0;

        for (char c : value.toCharArray()) {

            int valueIndex = BASE32.indexOf(c);

            if (valueIndex < 0) {
                throw new IllegalArgumentException(
                        "Invalid Base32 secret"
                );
            }

            buffer = (buffer << 5) | valueIndex;
            bits += 5;

            if (bits >= 8) {

                bits -= 8;

                output.write(
                        (buffer >> bits) & 0xff
                );
            }
        }

        return output.toByteArray();
    }

    public String provisioningUri(
            String email,
            String secret) {

        return "otpauth://totp/LoanApp:" + email
                + "?secret=" + secret
                + "&issuer=LoanApp"
                + "&algorithm=SHA1"
                + "&digits=6"
                + "&period=30";
    }
}
