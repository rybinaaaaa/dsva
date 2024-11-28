package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Objects;

import static rybina.ctu.bully.utils.ServerProperties.getHost;
import static rybina.ctu.bully.utils.ServerProperties.getPort;

public class ServerRegistry {

    public static String[] getNodeIdList() throws RemoteException {
        return Arrays.stream(getRegistry().list())
                .filter(id -> !id.equals(NodeBinder.class.getName()))
                .toArray(String[]::new);
    }

    public static Node getNodeById(String nodeId) throws RemoteException, NotBoundException {
        return (Node) getRegistry().lookup(nodeId);
    }

    public static Node addNodeToRegistry(int nodeId) throws RemoteException, NotBoundException {
        NodeBinder binder = (NodeBinder) getRegistry().lookup(NodeBinder.class.getName());
        Node node = binder.bindNode(nodeId);
        return node;
    }

    public static Registry getRegistry() throws RemoteException {
        return LocateRegistry.getRegistry(getHost(), getPort());
    }
}
