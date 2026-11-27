
package com.codex.filestorage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class UploadSessionService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String PURPOSE = "UPLOAD_COMPLETE";

    private final String secret;
    private final long ttlSeconds;

    public UploadSessionService(
            @Value("${app.upload-session.secret}") String secret,
            @Value("${app.upload-session.ttl-seconds:900}") long ttlSeconds
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                "Upload session secret must contain at least 32 characters"
            );
        }

        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(
            UUID fileId,
            UUID organizationId,
            UUID userId
    ) {
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;

        String payload = String.join("|",
                "v1",
                fileId.toString(),
                organizationId.toString(),
                userId.toString(),
                PURPOSE,
                String.valueOf(expiresAt)
        );

        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        String signature = sign(encodedPayload);

        return encodedPayload + "." + signature;
    }

    public boolean validateToken(
            String token,
            UUID expectedFileId,
            UUID expectedOrganizationId,
            UUID expectedUserId
    ) {
        try {
            String[] parts = token.split("\\.", -1);

            if (parts.length != 2) {
                return false;
            }

            String encodedPayload = parts[0];
            String suppliedSignature = parts[1];

            String expectedSignature = sign(encodedPayload);

            if (!MessageDigest.isEqual(
                    suppliedSignature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            String payload = new String(
                    Base64.getUrlDecoder().decode(encodedPayload),
                    StandardCharsets.UTF_8
            );

            String[] claims = payload.split("\\|", -1);

            if (claims.length != 6 || !claims[0].equals("v1")) {
                return false;
            }

            UUID fileId = UUID.fromString(claims[1]);
            UUID organizationId = UUID.fromString(claims[2]);
            UUID userId = UUID.fromString(claims[3]);
            String purpose = claims[4];
            long expiresAt = Long.parseLong(claims[5]);

            return fileId.equals(expectedFileId)
                    && organizationId.equals(expectedOrganizationId)
                    && userId.equals(expectedUserId)
                    && purpose.equals(PURPOSE)
                    && Instant.now().getEpochSecond() < expiresAt;

        } catch (Exception e) {
            return false;
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);

            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            ));

            byte[] signature = mac.doFinal(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signature);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not sign upload session token", e
            );
        }
    }
}
