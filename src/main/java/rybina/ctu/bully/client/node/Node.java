package rybina.ctu.bully.client.node;

import rybina.ctu.bully.utils.NodeInfo;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Consumer;

public interface Node extends FileManagerNode {

    void setCoordinator(NodeInfo coordinator) throws RemoteException;

    void becomeCoordinator() throws RemoteException, NotBoundException;

    void startElection() throws RemoteException, NotBoundException;

    void wakeUpElection(Node sender) throws RemoteException, NotBoundException;

    String getNodeId() throws RemoteException;

    void notifyAll(Consumer<Node> callback) throws RemoteException, NotBoundException;

    Node getCoordinator() throws RemoteException;

    void receiveLostStatus() throws RemoteException;

    void addNeighbor(NodeInfo neighbor) throws RemoteException;

    NodeInfo getNodeInfo() throws RemoteException;

    List<NodeInfo> getNeighbors() throws RemoteException;
}