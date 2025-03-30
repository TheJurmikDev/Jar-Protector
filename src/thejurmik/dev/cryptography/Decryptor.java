package thejurmik.dev.cryptography;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Decryptor {
    private static final int SALT_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 128;
    private static final String AES_MODE = "AES/GCM/NoPadding";

    private final String password;

    public Decryptor(String password) {
        this.password = password;
    }

    public byte[] decrypt(String encryptedData) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);

            int encryptedLength = combined.length - SALT_LENGTH - GCM_IV_LENGTH;
            byte[] encrypted = new byte[encryptedLength];
            System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, encrypted, 0, encryptedLength);

            Cipher cipher = Cipher.getInstance(AES_MODE);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decrypting error: ", e);
        }
    }
}
