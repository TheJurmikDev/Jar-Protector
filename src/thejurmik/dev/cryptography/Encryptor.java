package thejurmik.dev.cryptography;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Encryptor {
    private static final int SALT_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 128;
    private static final String AES_MODE = "AES/GCM/NoPadding";

    private final String password;
    private final SecureRandom random = new SecureRandom();

    public Encryptor(String password) {
        this.password = password;
    }

    public String encrypt(byte[] data) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(AES_MODE);
            byte[] iv = new byte[GCM_IV_LENGTH];
            random.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(data);

            byte[] combined = new byte[SALT_LENGTH + GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(salt, 0, combined, 0, SALT_LENGTH);
            System.arraycopy(iv, 0, combined, SALT_LENGTH, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, SALT_LENGTH + GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encrypting error: ", e);
        }
    }
}