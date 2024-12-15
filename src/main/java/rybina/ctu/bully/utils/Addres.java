package rybina.ctu.bully.utils;

public class Addres {
    private final int port;
    private final String host;

    public Addres(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
