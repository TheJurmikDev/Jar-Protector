---

# 🔒 JAR Encryption & Decryption Tool

## 🚀 Overview
This tool provides encryption and decryption functionalities for JAR files, allowing secure storage and execution without writing decrypted data to disk.

## 🛠️ Features
- 🔐 **Encrypt** JAR files using **AES/CBC/PKCS5Padding**
- 🔓 **Decrypt** and execute JAR files directly from memory
- 🚀 **Dynamic class loading** without extracting files
- 📁 **Supports embedded resources** in JAR files

### 🔧 Compilation
```bash
javac JarTool.java
jar cf JarTool.jar JarTool.class
```
### 🚀 Running the Tool
```bash
java -jar JarTool.jar encrypt myfile.jar
java -jar JarTool.jar load encrypted_myfile.jar
```
- `encrypt`: Encrypts the specified JAR file and saves it as `encrypted_<jarFile>`.
- `load`: Loads and executes an encrypted JAR file directly from memory.

## ⚙️ How It Works
- 🔑 **AES Encryption**: Uses AES with an initialization vector to secure JAR files.
- 🏗️ **InMemoryJarClassLoader**: Dynamically loads classes and resources from decrypted JARs.
- 📦 **Dependencies**: Uses `javax.crypto`, `java.util.jar`, and `java.nio.file` for encryption and file handling.

## 📚 Inspiration
This project was built using insights from **Stack Overflow** and **GitHub**. I aimed to create a secure way to encrypt and execute JAR files without extracting them.

---
