package rybina.ctu.bully.client.node;

import rybina.ctu.bully.client.FileManager;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());

    private final int nodeId;
    private int coordinatorId;
    private boolean isCoordinator;
    private final FileManager fileManager;
    private boolean electionStarted = false;
    private final Map<String, StringBuilder> fileSimulation = new HashMap<>();


    public NodeImpl(int nodeId) throws RemoteException {
        this.nodeId = nodeId;
        this.isCoordinator = false;
        this.coordinatorId = -1;
        this.fileManager = new FileManager(this);
    }

    @Override
    public void setCoordinatorId(int coordinatorId) {
        this.coordinatorId = coordinatorId;
    }

    @Override
    public void becomeCoordinator() throws RemoteException {
        setIsCoordinator(true);
        setCoordinatorId(nodeId);
        notifyAll(node -> {
            try {
                node.setIsCoordinator(false);
                node.setCoordinatorId(coordinatorId);
            } catch (RemoteException ignored) {
            }
        });
    }

    @Override
    public void startElection() throws RemoteException {
        if (electionStarted) return;

        logger.info("Node " + nodeId + " Starting election");
        boolean hasHigherPriority = true;

        for (String id : ServerRegistry.getNodeIdList()) {
            if (id.equals(String.valueOf(nodeId))) continue;
            try {
                Node node = ServerRegistry.getNodeById(id);
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

        electionStarted = false;
    }


    public int getNodeId() {
        return nodeId;
    }

    @Override
    public void notifyAll(Consumer<Node> callback) throws RemoteException {
        for (String id : ServerRegistry.getNodeIdList()) {
            if (id.equals(String.valueOf(nodeId))) continue;
            try {
                Node node = ServerRegistry.getNodeById(id);
                callback.accept(node);
            } catch (NotBoundException e) {
                logger.severe("Node with id " + id + " not found");
            }
        }
    }

    @Override
    public Node getCoordinator() throws RemoteException {
        try {
            return ServerRegistry.getNodeById(String.valueOf(coordinatorId));
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

    public void initCoordinator() throws RemoteException {
        if (coordinatorId == -1) {
            logger.info("The node coordinator is uninitialized. Binding the coordinator...");
            String[] nodeIdList = ServerRegistry.getNodeIdList();
            if (nodeIdList.length == 1) {
                logger.info("The node is first in registry. Initialized node with id " + nodeId + " as coordinator.");
                becomeCoordinator();
                return;
            }

            logger.info("Getting coordinator from existing nodes");
            for (String id : nodeIdList) {
                if (id.equals(String.valueOf(nodeId))) continue;

                try {
                    Node node = ServerRegistry.getNodeById(id);

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

    @Override
    public boolean createFile(String filename) throws RemoteException {
        if (fileSimulation.containsKey(filename)) {
            logger.warning("File already exists: " + filename);
            return false;
        }
        fileSimulation.put(filename, new StringBuilder());
        return true;
    }

    @Override
    public boolean deleteFile(String filename) throws RemoteException {
        if (!fileSimulation.containsKey(filename)) {
            logger.warning("File does not exist: " + filename);
            return false;
        }

        fileSimulation.remove(filename);
        logger.info("File was successfully deleted: " + filename);
        return true;
    }

    @Override
    public boolean writeToFile(String filename, String content) throws RemoteException {
        if (!fileSimulation.containsKey(filename)) {
            logger.warning("File does not exist: " + filename);
            return false;
        }

        fileSimulation.get(filename).append(content).append("\n");
        logger.info("Data was successfully written to file: " + filename);
        return true;
    }

    @Override
    public String readFromFile(String filename) throws RemoteException {
        if (!fileSimulation.containsKey(filename)) {
            logger.warning("File does not exist: " + filename);
            return null;
        }

        return fileSimulation.get(filename).toString();
    }


    public void setIsCoordinator(boolean coordinator) {
        coordinatorId = this.nodeId;
        isCoordinator = coordinator;
    }
}
