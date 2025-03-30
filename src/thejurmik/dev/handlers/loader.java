package thejurmik.dev.handlers;

import thejurmik.dev.cryptography.Decryptor;
import thejurmik.dev.loaders.InMemoryClassLoader;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class loader {

    public static void handleLoading(String fileName) throws Exception {
        String encryptedContent = Files.readString(new File(fileName).toPath(), StandardCharsets.UTF_8);
        Decryptor decryptor = new Decryptor("1A6F3E8D1A6F3E8D");
        byte[] decryptedJar = decryptor.decrypt(encryptedContent);

        InMemoryClassLoader cl = new InMemoryClassLoader(decryptedJar);
        try {
            Class<?> mainClass = cl.loadClass("MAIN CLASS");
            mainClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
        } finally {
            if (cl instanceof AutoCloseable) {
                ((AutoCloseable) cl).close();
            }
        }
    }
}
