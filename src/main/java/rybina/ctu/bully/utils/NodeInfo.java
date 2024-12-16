package rybina.ctu.bully.utils;

import java.io.Serializable;
import java.util.Objects;

public class NodeInfo implements Serializable {

    private String hostname;
    private int port;
    private String nodeId;
    private Simulation.PermissionRole role = Simulation.PermissionRole.GUEST;

    public NodeInfo(String hostname, int port, String nodeId, Simulation.PermissionRole role) {
        this.hostname = hostname;
        this.port = port;
        this.nodeId = nodeId;
        this.role = role;
    }

    public NodeInfo(String hostname, int port, String nodeId) {
        this.hostname = hostname;
        this.port = port;
        this.nodeId = nodeId;
    }

    public NodeInfo() {
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

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setRole(Simulation.PermissionRole role) {
        this.role = role;
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

    @Override
    public String toString() {
        return "NodeInfo{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", nodeId='" + nodeId + '\'' +
                ", role=" + role +
                '}';
    }
}
