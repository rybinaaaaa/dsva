package rybina.ctu.bully.utils;

import java.io.InputStream;
import java.util.Properties;

public class ServerProperties {
    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = ServerProperties.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("server.properties not found in classpath");
            }
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHost() {
        return (String) properties.get("server.host");
    }

    public static int getPort() {
        return Integer.parseInt(properties.get("server.port").toString());
    }
}
