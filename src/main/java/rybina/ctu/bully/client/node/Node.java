package rybina.ctu.bully.client.node;

import rybina.ctu.bully.utils.NodeInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public interface Node extends Remote {

    void setCoordinator(NodeInfo coordinator) throws RemoteException;

    void becomeCoordinator() throws RemoteException;

    void startElection() throws RemoteException;

    void wakeUpElection(Node sender) throws RemoteException;

    String getNodeId() throws RemoteException;

    void notifyAll(Consumer<Node> callback) throws RemoteException;

    Node getCoordinator() throws RemoteException;

    void receiveLostStatus() throws RemoteException;

    void addNeighbor(NodeInfo neighbor) throws RemoteException;

    NodeInfo getNodeInfo() throws RemoteException;

    List<NodeInfo> getNeighbors() throws RemoteException;

    String getFile() throws RemoteException;

    String setFile(String file) throws RemoteException, InterruptedException, TimeoutException;

    void setFileByLeader(String file) throws RemoteException;

    void setElectionStarted(boolean electionStarted) throws RemoteException;
}