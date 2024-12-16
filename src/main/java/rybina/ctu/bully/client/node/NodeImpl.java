package rybina.ctu.bully.client.node;

import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.ServerRegistry;
import rybina.ctu.bully.utils.Simulation;
import rybina.ctu.bully.utils.Simulation.PermissionRole;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static rybina.ctu.bully.utils.Simulation.enrichFakeData;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());

    private final NodeInfo nodeInfo;

    private final String nodeId;
    private NodeInfo coordinator = null;
    private boolean isCoordinator = false;
    private boolean isCandidate = false;
    private boolean electionStarted = false;

    private final List<NodeInfo> neighbors = new ArrayList<>();
    private final HashMap<String, Simulation.FileInfo> fileSystem = enrichFakeData();


    public NodeImpl(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
        this.nodeInfo = nodeInfo;
        this.nodeId = nodeInfo.getNodeId();
        bindToServer();
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void becomeCoordinator() throws RemoteException, NotBoundException {
        setCandidate(false);
        setCoordinator(getNodeInfo());
        notifyAll(node -> {
            try {
                node.setCoordinator(getNodeInfo());
            } catch (RemoteException ignored) {
            }
        });
    }

    @Override
    public void startElection() throws RemoteException, NotBoundException {
        logger.info("Node " + nodeId + ": Starts election");
        setCandidate(true);
        Node sender = this;

        notifyAll(new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                try {
                    if (Integer.parseInt(node.getNodeId()) > Integer.parseInt(nodeId)) {
                        node.wakeUpElection(sender);
                    }
                } catch (RemoteException | NotBoundException e) {
                    logger.severe("Node " + nodeId + ": Error during notifying nodes election");
                }
            }
        });

        if (isCandidate) {
            logger.info("Node " + nodeId + ": becomes a leader");
            becomeCoordinator();
        }

        electionStarted = false;
    }

    @Override
    public void wakeUpElection(Node sender) throws RemoteException, NotBoundException {
        if (electionStarted) return;
        electionStarted = true;
        if (Integer.parseInt(sender.getNodeId()) > Integer.parseInt(nodeId)) {
            sender.receiveLostStatus();
        }

        startElection();
    }

    @Override
    public void notifyAll(Consumer<Node> callback) throws RemoteException, NotBoundException {
        for (NodeInfo nodeInfo : neighbors) {
            if (nodeId.equals(nodeInfo.getNodeId())) {
                throw new RuntimeException("Implementation error! Node should not contain itself");
            }
            Node node = ServerRegistry.getNode(nodeInfo);
            callback.accept(node);
        }
    }

    @Override
    public Node getCoordinator() throws RemoteException, NotBoundException {
        if (coordinator == null) {
            logger.warning("Node " + nodeId + ": have null coordinator. Initializing coordinator");
            startElection();
            return getCoordinator();
        }

        try {
            return ServerRegistry.getNode(coordinator);
        } catch (NotBoundException e) {
            logger.warning("Node " + nodeId + ": Looks like coordinator is unavailable. Start election...");
            startElection();
            return getCoordinator();
        }
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    @Override
    public void receiveLostStatus() throws RemoteException {
        setCandidate(false);
    }

    @Override
    public void addNeighbor(NodeInfo nodeInfo) throws RemoteException {
        neighbors.add(nodeInfo);
    }

    public void setCandidate(boolean candidate) {
        isCandidate = candidate;
    }

    public void bindToServer() throws RemoteException, NotBoundException {
        String hostname = nodeInfo.getHostname();
        int port = nodeInfo.getPort();

        System.setProperty("java.rmi.server.hostname", hostname);

        logger.info("Preparing server...");

        Registry registry = null;

        try {
            registry = LocateRegistry.getRegistry(port);
            registry.list();
            logger.info("Connected to existing registry.");
        } catch (RemoteException e) {
            logger.info("No existing registry found. Creating a new one...");
            registry = LocateRegistry.createRegistry(port);
        }
        registry.rebind(nodeId, this);
        logger.info("Node " + nodeId + ": Node is running with server at " + hostname + ":" + nodeInfo.getPort());
    }

    public void showAllNeighbours() {
        System.out.println("Node with id: " + getNodeId() + " has neighbours:");
        neighbors.forEach(System.out::println);
    }

    @Override
    public NodeInfo getNodeInfo() throws RemoteException {
        return nodeInfo;
    }

    public void setCoordinator(NodeInfo coordinator) throws RemoteException {
        isCoordinator = coordinator == nodeInfo;
        this.coordinator = coordinator;
    }

    public void bindNode(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
        addNeighbor(nodeInfo);
        ServerRegistry.getNode(nodeInfo).addNeighbor(getNodeInfo());
    }

    public void bindToNode(NodeImpl node) throws NotBoundException, RemoteException {
        logger.info("Binding to node with id: " + node.getNodeId() + " node with id " + nodeId);

        for (NodeInfo neighbor : node.getNeighbors()) {
            bindNode(neighbor);
        }

        bindNode(node.getNodeInfo());
        if (node.coordinator == null) {
            startElection();
        } else {
            setCoordinator(node.coordinator);
        }
    }

    @Override
    public String getContent(String fileName, NodeInfo sender) throws NotBoundException, RemoteException {
        if (isCoordinator) {
            if (!fileSystem.containsKey(fileName)) {
                throw new RuntimeException("File " + fileName + " not found");
            }
            if (!fileSystem.get(fileName).getRoles().contains(sender.getRole())) {
                throw new RuntimeException("Role " + sender.getRole() + " is not allowed to access file " + fileName);
            }
            return fileSystem.get(fileName).getContent();
        }

        return getCoordinator().getContent(fileName, sender);
    }

    @Override
    public String getContent(String fileName) throws RemoteException, NotBoundException {
        return getContent(fileName, this.getNodeInfo());
    }

    @Override
    public ArrayList<Simulation.FileInfo> getAvailableFiles() throws RemoteException {
        return new ArrayList<>(this.fileSystem.values());
    }

    @Override
    public List<NodeInfo> getNeighbors() {
        return neighbors;
    }


}
