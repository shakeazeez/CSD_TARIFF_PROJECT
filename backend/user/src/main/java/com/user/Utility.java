package com.user;

import io.github.cdimascio.dotenv.Dotenv;

public class Utility {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .filename(".env")
            .load();

    public static String getEnvOrDotenv(String key) {
        String value = System.getenv(key);
        if (value != null)
            return value;
        try {
            Dotenv dotenv = Dotenv.load();
            return dotenv.get(key);
        } catch (Exception e) {
            return null;
        }
    }
}
