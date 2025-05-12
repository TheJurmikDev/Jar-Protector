package thejurmik.dev;

import static thejurmik.dev.handlers.Encryption.handleEncryption;
import static thejurmik.dev.handlers.loader.handleLoading;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                printUsage();
                return;
            }

            Mode mode = Mode.fromString(args[0]);
            String fileName = args[1];

            switch (mode) {
                case ENCRYPT:
                    handleEncryption(fileName);
                    break;
                case LOAD:
                    handleLoading(fileName);
                    break;
                default:
                    System.out.println("Invalid Mode: " + args[0]);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar Cryptor.jar <encrypt|load> <path>");
    }

    enum Mode {
        ENCRYPT, LOAD;

        public static Mode fromString(String mode) {
            return Mode.valueOf(mode.toUpperCase());
        }
    }
}
