package rybina.ctu.bully.client.node;

import rybina.ctu.bully.client.FileManager;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.function.Consumer;

public interface Node extends Remote {

    void initCoordinator() throws RemoteException;

    void setCoordinatorId(int coordinatorId) throws RemoteException;

    void setIsCoordinator(boolean isCoordinator) throws RemoteException;

    void becomeCoordinator() throws RemoteException;

    void startElection() throws RemoteException;

    int getNodeId() throws RemoteException;

    void notifyAll(Consumer<Node> callback) throws RemoteException;

    Node getCoordinator() throws RemoteException;

    FileManager getFileManager() throws RemoteException;
}