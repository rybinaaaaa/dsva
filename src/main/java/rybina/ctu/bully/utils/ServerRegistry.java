package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class ServerRegistry {
    private final Registry registry;

    public ServerRegistry(Registry registry) {
        this.registry = registry;
    }

    public String[] getNodeIdList() throws RemoteException {
        return registry.list();
    }

    public Node getNodeById(String nodeId) throws RemoteException, NotBoundException {
        return  (Node) registry.lookup(nodeId);
    }
}
