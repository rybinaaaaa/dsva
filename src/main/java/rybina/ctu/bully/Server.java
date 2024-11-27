package rybina.ctu.bully;

import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.client.node.NodeImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

import static rybina.ctu.bully.utils.ServerProperties.getHost;
import static rybina.ctu.bully.utils.ServerProperties.getPort;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static Registry registry;

    public static void startServer() {
        try {
            System.setProperty("java.rmi.server.hostname", getHost());
            logger.info("Preparing server...");
            registry = LocateRegistry.createRegistry(getPort());
            logger.info("Server ready!");
        } catch (RemoteException e) {
            logger.severe("Server exception: " + e);
        }
    }

    public static Node addNodeToRegistry(int nodeId) throws RemoteException {
        if (registry == null) {
            logger.severe("Server is not started yet. Registry not initialized");
        }

        logger.info("Starting node with id: " + nodeId);
        NodeImpl node = new NodeImpl(nodeId, registry);
        logger.info("Looking for server");
        registry.rebind(String.valueOf(nodeId), node);
        node.run();
        return node;
    }
}
