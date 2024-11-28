package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.client.node.NodeImpl;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

public class ServerRegistry extends UnicastRemoteObject implements Remote {
    private final Registry registry;
    private static final Logger logger = Logger.getLogger(ServerRegistry.class.getName());

    public static ServerRegistry get(Registry registry) throws RemoteException, NotBoundException {
        return  (ServerRegistry) registry.lookup(ServerRegistry.class.getName());
    }

    public static ServerRegistry bindServerRegistry(Registry registry) throws RemoteException {
        ServerRegistry serverRegistry = new ServerRegistry(registry);
        registry.rebind(ServerRegistry.class.getName(), serverRegistry);
        return serverRegistry;
    }

    public String[] getNodeIdList() throws RemoteException {
        return registry.list();
    }

    public Node getNodeById(String nodeId) throws RemoteException, NotBoundException {
        return  (Node) registry.lookup(nodeId);
    }

    public Node addNodeToRegistry(int nodeId) throws RemoteException {
        if (registry == null) {
            logger.severe("Registry not initialized");
            return null;
        }

        logger.info("Starting node with id: " + nodeId);
        NodeImpl node = new NodeImpl(nodeId, this);
        logger.info("Looking for server");
        registry.rebind(String.valueOf(nodeId), node);
        node.run();
        return node;
    }

    public ServerRegistry(Registry registry) throws RemoteException {
        this.registry = registry;
    }
}
