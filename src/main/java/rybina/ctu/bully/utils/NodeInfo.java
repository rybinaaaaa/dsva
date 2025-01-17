package rybina.ctu.bully.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Objects;

public class NodeInfo implements Serializable {

    private String hostname;
    private int port;
    private String nodeId;
    private String coordinatorId;

    @JsonIgnore
    private NodeInfo coordinator;

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


    public NodeInfo getCoordinator() {
        return coordinator;
    }

    public String getCoordinatorId() {
        return coordinatorId;
    }

    public void setCoordinator(NodeInfo coordinator) {
        this.coordinator = coordinator;
        this.coordinatorId = coordinator.getNodeId();
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

    @Override
    public String toString() {
        return "NodeInfo{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", nodeId='" + nodeId + '\'' +
                ", coordinator=" + coordinatorId +
                '}';
    }
}
