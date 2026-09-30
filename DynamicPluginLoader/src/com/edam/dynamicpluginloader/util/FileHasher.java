package com.edam.dynamicpluginloader.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class FileHasher {

    private static final String HASH_ALGORITHM = "SHA-256";

    public static String hashSmallFile(Path jarPath) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(jarPath);
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] encodedHash = digest.digest(bytes);
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " algorithm is not available", e);
        }
    }

    public static String hashBigFile(Path jarPath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            try (BufferedInputStream buffered = new BufferedInputStream(
                    Files.newInputStream(jarPath))) {

                byte[] block = new byte[8192]; // 8 KB Buffer
                int bytesRead;
                while ((bytesRead = buffered.read(block)) != -1) {
                    digest.update(block, 0, bytesRead);
                }
                byte[] encodedHash = digest.digest();
                return HexFormat.of().formatHex(encodedHash);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " algorithm is not available", e);
        }
    }
}
