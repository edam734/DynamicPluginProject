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
    private static final long SIZE_THRESHOLD_BYTES = 50 * 1024 * 1024; // 50 MiB

    public static String calculateHash(Path jarPath) throws IOException {
        long fileSize = Files.size(jarPath);
        if (fileSize < SIZE_THRESHOLD_BYTES) {
            return FileHasher.hashSmallFile(jarPath);
        } else {
            return FileHasher.hashBigFile(jarPath);
        }
    }

    private static String hashSmallFile(Path jarPath) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(jarPath);
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] encodedHash = digest.digest(bytes);
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " algorithm is not available", e);
        }
    }

    private static String hashBigFile(Path jarPath) throws IOException {
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
