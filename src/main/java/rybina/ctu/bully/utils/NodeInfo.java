package rybina.ctu.bully.utils;

import java.io.Serializable;

public class NodeInfo implements Serializable {

    private final String hostname;
    private final int port;
    private final String nodeId;

    public NodeInfo(String hostname, int port, String nodeId) {
        this.hostname = hostname;
        this.port = port;
        this.nodeId = nodeId;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getNodeId() {
        return nodeId;
    }
}
