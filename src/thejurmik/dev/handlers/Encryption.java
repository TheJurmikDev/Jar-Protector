package thejurmik.dev.handlers;

import thejurmik.dev.cryptography.Encryptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Encryption {

    public static void handleEncryption(String fileName) throws IOException {
        byte[] fileContent = Files.readAllBytes(new File(fileName).toPath());
        Encryptor encryptor = new Encryptor("1A6F3E8D1A6F3E8D");
        String encryptedContent = encryptor.encrypt(fileContent);
        Files.writeString(new File("encrypted_" + fileName).toPath(), encryptedContent);
        System.out.println("File sucessfully encrypted");
    }
}
