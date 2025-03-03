import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarTool {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java JarTool <encrypt|load> <jarFile>");
            return;
        }

        String mode = args[0];
        String fileName = args[1];

        if (mode.equalsIgnoreCase("encrypt")) {
            byte[] jarBytes = Files.readAllBytes(Paths.get(fileName));
            String jarContent = new String(jarBytes, StandardCharsets.ISO_8859_1);

            Encryptor encryptor = new Encryptor();
            String encryptedJar = encryptor.encrypt(jarContent);

            String outputFileName = "encrypted_" + fileName;
            Files.writeString(Paths.get(outputFileName), encryptedJar);
            System.out.println("JAR file has been saved like: " + outputFileName);

        } else if (mode.equalsIgnoreCase("load")) {
            File file = new File(fileName);
            URL fileUrl = file.toURI().toURL();

            try (InputStream is = fileUrl.openStream()) {
                String encryptedJar = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                Decryptor decryptor = new Decryptor();
                String decryptedJarContent = decryptor.decrypt(encryptedJar);

                byte[] jarBytes = decryptedJarContent.getBytes(StandardCharsets.ISO_8859_1);

                InMemoryJarClassLoader classLoader = new InMemoryJarClassLoader(jarBytes);
                Class<?> mainClass = classLoader.loadClass("Start");
                mainClass.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
            }
        } else {
            System.out.println("Unknown mode: " + mode);
        }
    }

    public static class Encryptor {
        private static final String KEY = "1A6F3E8D"; // random key
        private static final String INIT_VECTOR = "RandomInitVector"; // random key
        private static final String AES_MODE = "AES/CBC/PKCS5Padding";

        public String encrypt(String input) {
            if (input == null || input.isEmpty()) {
                return input;
            }
            try {
                Cipher cipher = Cipher.getInstance(AES_MODE);
                SecretKeySpec keySpec = new SecretKeySpec(padKey().getBytes(), "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(INIT_VECTOR.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                byte[] encryptedBytes = cipher.doFinal(input.getBytes());
                return Base64.getEncoder().encodeToString(encryptedBytes);

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
        }

        private String padKey() {
            StringBuilder paddedKey = new StringBuilder(KEY);
            while (paddedKey.length() < 16) {
                paddedKey.append(" ");
            }
            return paddedKey.substring(0, 16);
        }
    }

    public static class Decryptor {
        private static final String KEY = "1A6F3E8D"; // random key
        private static final String INIT_VECTOR = "RandomInitVector"; // random key
        private static final String AES_MODE = "AES/CBC/PKCS5Padding";

        public String decrypt(String encryptedInput) {
            if (encryptedInput == null || encryptedInput.isEmpty()) {
                return encryptedInput;
            }
            try {
                Cipher cipher = Cipher.getInstance(AES_MODE);
                SecretKeySpec keySpec = new SecretKeySpec(padKey().getBytes(), "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(INIT_VECTOR.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                byte[] decodedBytes = Base64.getDecoder().decode(encryptedInput);
                byte[] decryptedBytes = cipher.doFinal(decodedBytes);
                return new String(decryptedBytes);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
        }

        private String padKey() {
            StringBuilder paddedKey = new StringBuilder(KEY);
            while (paddedKey.length() < 16) {
                paddedKey.append(" ");
            }
            return paddedKey.substring(0, 16);
        }
    }

    public static class InMemoryJarClassLoader extends ClassLoader {
        private final Map<String, byte[]> classBytesMap = new HashMap<>();
        private final Map<String, byte[]> resourceBytesMap = new HashMap<>();

        public InMemoryJarClassLoader(byte[] jarBytes) throws IOException {
            try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes))) {
                JarEntry entry;
                while ((entry = jis.getNextJarEntry()) != null) {
                    byte[] data = jis.readAllBytes();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace('/', '.').replace(".class", "");
                        classBytesMap.put(className, data);
                    } else {
                        resourceBytesMap.put(entry.getName(), data);
                    }
                }
            }
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] classBytes = classBytesMap.get(name);
            if (classBytes == null) {
                throw new ClassNotFoundException("Třída " + name + " nebyla nalezena v dešifrovaném JAR.");
            }
            return defineClass(name, classBytes, 0, classBytes.length);
        }

        @Override
        public URL getResource(String name) {
            if (resourceBytesMap.containsKey(name)) {
                try {
                    File tempFile = File.createTempFile("resource", null);
                    tempFile.deleteOnExit();
                    Files.write(tempFile.toPath(), resourceBytesMap.get(name));
                    return tempFile.toURI().toURL();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return super.getResource(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (resourceBytesMap.containsKey(name)) {
                return new ByteArrayInputStream(resourceBytesMap.get(name));
            }
            return super.getResourceAsStream(name);
        }
    }
}