package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.NodeBinder;
import rybina.ctu.bully.client.node.Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import static rybina.ctu.bully.utils.ServerProperties.getHost;
import static rybina.ctu.bully.utils.ServerProperties.getPort;

public class ServerRegistry {

    public static Node getNode(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
        return (Node) LocateRegistry.getRegistry(nodeInfo.getHostname(), nodeInfo.getPort())
                .lookup(nodeInfo.getNodeId());
    }

    public static Registry getRegistry() throws RemoteException {
        return LocateRegistry.getRegistry(getHost(), getPort());
    }
}
