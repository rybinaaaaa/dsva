package rybina.ctu.bully.utils;

import java.io.Serializable;
import java.util.Objects;

public class NodeInfo implements Serializable {

    private String hostname;
    private int port;
    private String nodeId;
    private NodeInfo coordinator;
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

    public NodeInfo getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(NodeInfo coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(nodeId, nodeInfo.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, port, nodeId, coordinator, role);
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", nodeId='" + nodeId + '\'' +
                ", coordinator=" + coordinator.getNodeId() +
                ", role=" + role +
                '}';
    }
}
