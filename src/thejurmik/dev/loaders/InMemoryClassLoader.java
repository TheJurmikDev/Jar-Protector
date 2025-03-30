package thejurmik.dev.loaders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class InMemoryClassLoader extends ClassLoader {
    private final Map<String, byte[]> classes = new HashMap<>();
    private final Map<String, byte[]> resources = new HashMap<>();

    public InMemoryClassLoader(byte[] jarData) throws IOException {
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarData))) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                byte[] data = jis.readAllBytes();
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    String className = name.replace('/', '.').replace(".class", "");
                    classes.put(className, data);
                } else {
                    resources.put(name, data);
                }
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classData = classes.get(name);
        if (classData == null) {
            throw new ClassNotFoundException("Class " + name + " not found");
        }
        return defineClass(name, classData, 0, classData.length);
    }

    @Override
    public URL getResource(String name) {
        byte[] data = resources.get(name);
        if (data != null) {
            return getResourceURL(data);
        }
        return super.getResource(name);
    }

    private URL getResourceURL(byte[] data) {
        try {
            File temp = File.createTempFile("resource", null);
            temp.deleteOnExit();
            Files.write(temp.toPath(), data);
            return temp.toURI().toURL();
        } catch (IOException e) {
            throw new RuntimeException("Error loading resource", e);
        }
    }
}