package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class ServerRegistry {

    public static Node getNode(String hostname, int port, String id) throws RemoteException, NotBoundException {
        return (Node) LocateRegistry.getRegistry(hostname, port)
                .lookup(id);
    }

    public static Node getNode(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
        return (Node) LocateRegistry.getRegistry(nodeInfo.getHostname(), nodeInfo.getPort())
                .lookup(nodeInfo.getNodeId());
    }
}
