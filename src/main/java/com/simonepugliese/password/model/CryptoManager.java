package com.simonepugliese.password.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

public class CryptoManager {
    private static final Logger logger = LoggerFactory.getLogger(CryptoManager.class);

    public static final String ALGO = "AES/GCM/NoPadding";
    private static final int KEY_LEN = 256;
    private static final int IV_LEN = 12;
    private static final int TAG_LEN = 128;
    private static final int SALT_LEN = 16;
    private static final int ITER = 500_000;

    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITER, KEY_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        logger.info("Derivata chiave");
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LEN];
        new SecureRandom().nextBytes(salt);
        logger.info("Generato salt");
        return salt;
    }

    public static byte[] generateIV() {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);
        logger.info("Generato IV");
        return iv;
    }

    public static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LEN, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        logger.info("Criptata password");
        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LEN, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        logger.info("Decriptata password");
        return cipher.doFinal(ciphertext);
    }
}

