package rybina.ctu.bully.client.node;

import rybina.ctu.bully.client.FileManager;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static rybina.ctu.bully.utils.ServerProperties.getHost;

public class NodeImpl extends UnicastRemoteObject implements Node, Runnable {

    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());

    private final int nodeId;
    private int coordinatorId;
    private boolean isCoordinator;
    private ServerRegistry registry;
    private final FileManager fileManager;


    public NodeImpl(int nodeId, Registry registry) throws RemoteException {
        this.nodeId = nodeId;
        this.isCoordinator = false;
        this.coordinatorId = -1;
        this.fileManager = new FileManager(this);
        this.registry = new ServerRegistry(registry);
    }

    @Override
    public void setCoordinatorId(int coordinatorId) {
        this.coordinatorId = coordinatorId;
    }

    @Override
    public void becomeCoordinator() throws RemoteException {
        setIsCoordinator(true);
        notifyAll(node -> {
            try {
                node.setCoordinatorId(coordinatorId);
            } catch (RemoteException ignored) {
            }
        });
    }

    @Override
    public void startElection() throws RemoteException {
        logger.info("Node " + nodeId + " Starting election");
        boolean hasHigherPriority = true;

        for (String id : registry.getNodeIdList()) {
            if (id.equals(String.valueOf(nodeId))) continue;
            try {
                Node node = registry.getNodeById(id);
                if (node.getNodeId() > nodeId) {
                    hasHigherPriority = false;
                    node.startElection();
                }
            } catch (NotBoundException e) {
                logger.severe("Node with id " + id + " not found");
            }
        }

        if (hasHigherPriority) {
            logger.info("Node " + nodeId + " becomes a leader");
            becomeCoordinator();
        }
    }


    public int getNodeId() {
        return nodeId;
    }

    @Override
    public void notifyAll(Consumer<Node> callback) throws RemoteException {
        for (String id : registry.getNodeIdList()) {
            if (id.equals(String.valueOf(nodeId))) continue;
            try {
                Node node = registry.getNodeById(id);
                callback.accept(node);
            } catch (NotBoundException e) {
                logger.severe("Node with id " + id + " not found");
            }
        }
    }

    @Override
    public Node getCoordinator() throws RemoteException {
        try {
            return registry.getNodeById(String.valueOf(coordinatorId));
        } catch (NotBoundException e) {
            logger.warning("Leader with id " + coordinatorId + " not found. Reevaluatind leader...");
            this.startElection();
            return this.getCoordinator();
        }
    }

    @Override
    public FileManager getFileManager() throws RemoteException {
        return this.fileManager;
    }

    private void initCoordinator() throws RemoteException {
        logger.info("The node coordinator is uninitialized. Binding the coordinator...");
        if (coordinatorId == -1) {
            String[] nodeIdList = registry.getNodeIdList();
            if (nodeIdList.length == 1) {
                logger.info("The node is first in registry. Initialized node with id " + nodeId + " as coordinator.");
                becomeCoordinator();
                return;
            }

            logger.info("Getting coordinator from existing nodes");
            for (String id : nodeIdList) {
                try {
                    Node node = registry.getNodeById(id);

//                    The getCoordinator is null-safe
                    Node coordinator = node.getCoordinator();
                    setCoordinatorId(coordinator.getNodeId());
                } catch (NotBoundException ignored) {
                }
            }
        }
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public void setIsCoordinator(boolean coordinator) {
        coordinatorId = this.nodeId;
        isCoordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            this.initCoordinator();
        } catch (RemoteException e) {
            logger.severe("Node running error " + e);
            throw new RuntimeException(e);
        }

        logger.info("Node " + nodeId + " is running on host " + getHost());
    }
}
