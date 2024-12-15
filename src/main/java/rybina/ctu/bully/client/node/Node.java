package rybina.ctu.bully.client.node;

import rybina.ctu.bully.utils.NodeInfo;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.function.Consumer;

public interface Node extends Remote {

    void setCoordinator(NodeInfo coordinator) throws RemoteException;

    void setIsCoordinator(boolean isCoordinator) throws RemoteException;

    void becomeCoordinator() throws RemoteException, NotBoundException;

    void startElection() throws RemoteException, NotBoundException;

    void wakeUpElection(Node sender) throws RemoteException, NotBoundException;

    String getNodeId() throws RemoteException;

    void notifyAll(Consumer<Node> callback) throws RemoteException, NotBoundException;

    Node getCoordinator() throws RemoteException, NotBoundException;

    boolean isCoordinator() throws RemoteException;

    void receiveLostStatus() throws RemoteException;

    void addNeighbor(NodeInfo neighbor) throws RemoteException, NotBoundException;

    void showAllNodes() throws RemoteException;

    NodeInfo getNodeInfo() throws RemoteException;
}