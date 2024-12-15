package rybina.ctu.bully.utils;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "NodeInfo{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return port == nodeInfo.port && Objects.equals(hostname, nodeInfo.hostname) && Objects.equals(nodeId, nodeInfo.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, port, nodeId);
    }
}
