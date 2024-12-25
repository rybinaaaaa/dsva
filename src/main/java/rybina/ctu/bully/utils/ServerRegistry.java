package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRegistry {

    private static final Logger logger = Logger.getLogger(ServerRegistry.class.getName());

    public static Node getNode(String hostname, int port, String id) {
        try {
            return (Node) LocateRegistry.getRegistry(hostname, port).lookup(id);
        } catch (NotBoundException e) {
            logger.log(Level.WARNING, "Node with ID " + id + " is not bound in the registry at " + hostname + ":" + port);
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "Failed to connect to RMI registry at " + hostname + ":" + port);
        }
        return null;
    }

    public static Node getNode(NodeInfo nodeInfo) {
        try {
            return (Node) LocateRegistry.getRegistry(nodeInfo.getHostname(), nodeInfo.getPort())
                    .lookup(nodeInfo.getNodeId());
        } catch (NotBoundException e) {
            logger.log(Level.WARNING, "Node with ID " + nodeInfo.getNodeId() + " is not bound in the registry at "
                    + nodeInfo.getHostname() + ":" + nodeInfo.getPort());
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "Failed to connect to RMI registry at " + nodeInfo.getHostname() + ":" + nodeInfo.getPort());
        }
        return null;
    }

    public static boolean removeNode(String hostname, int port, String id) {
        try {
            LocateRegistry.getRegistry(hostname, port).unbind(id);
            logger.info("Node with ID " + id + " removed from RMI registry at " + hostname + ":" + port);
            return true;
        } catch (NotBoundException e) {
            logger.log(Level.WARNING, "Node with ID " + id + " is not bound in the registry at " + hostname + ":" + port);
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "Failed to connect to RMI registry at " + hostname + ":" + port);
        }
        return false;
    }

    public static boolean removeNode(NodeInfo nodeInfo) {
        try {
            LocateRegistry.getRegistry(nodeInfo.getHostname(), nodeInfo.getPort()).unbind(nodeInfo.getNodeId());
            logger.info("Node with ID " + nodeInfo.getNodeId()+ " removed from RMI registry at " + nodeInfo.getHostname() + ":" + nodeInfo.getPort());
            return true;
        } catch (NotBoundException e) {
            logger.log(Level.WARNING, "Node with ID " +  nodeInfo.getNodeId() + " is not bound in the registry at " + nodeInfo.getHostname() + ":" + nodeInfo.getPort());
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "Failed to connect to RMI registry at " + nodeInfo.getHostname() + ":" + nodeInfo.getPort());
        }
        return false;
    }
}
