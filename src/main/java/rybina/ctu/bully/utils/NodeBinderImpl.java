package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.client.node.NodeImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import static rybina.ctu.bully.utils.ServerProperties.getHost;
import static rybina.ctu.bully.utils.ServerProperties.getPort;

public class NodeBinderImpl extends UnicastRemoteObject implements NodeBinder {
    public static final Logger logger = Logger.getLogger(NodeBinderImpl.class.getName());

    public NodeBinderImpl() throws RemoteException {
    }

    @Override
    public Node bindNode(Integer nodeId) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(getHost(), getPort());
        if (registry == null) {
            logger.severe("Registry not initialized");
            return null;
        }

        logger.info("Starting node with id: " + nodeId);
        NodeImpl node = new NodeImpl(nodeId);
        logger.info("Looking for server to bind node with id: " + nodeId);
        registry.rebind(String.valueOf(nodeId), node);
        node.initCoordinator();
        return node;
    }
}
