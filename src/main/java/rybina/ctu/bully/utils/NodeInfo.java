package rybina.ctu.bully.utils;

import java.io.Serializable;
import java.util.Objects;

public class NodeInfo implements Serializable {

    private final String hostname;
    private final int port;
    private final String nodeId;
    private final Simulation.PermissionRole role;

    public NodeInfo(String hostname, int port, String nodeId, Simulation.PermissionRole role) {
        this.hostname = hostname;
        this.port = port;
        this.nodeId = nodeId;
        this.role = role;
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

    public Simulation.PermissionRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", nodeId='" + nodeId + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return port == nodeInfo.port && Objects.equals(hostname, nodeInfo.hostname) && Objects.equals(nodeId, nodeInfo.nodeId) && role == nodeInfo.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, port, nodeId, role);
    }
}
