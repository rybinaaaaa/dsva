package rybina.ctu.bully.client.node;

import rybina.ctu.bully.RestController;
import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.ServerRegistry;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());

    private final NodeInfo nodeInfo;

    private final ReentrantLock lock = new ReentrantLock();
    private String file = "";
    private final static int WAITING_LIMIT = 5;

    private final String nodeId;
    private boolean isCoordinator = false;
    private boolean isCandidate = false;
    private boolean electionStarted = false;

    private final List<NodeInfo> neighbors = new ArrayList<>();


    public NodeImpl(NodeInfo nodeInfo) throws RemoteException {
        this.nodeInfo = nodeInfo;
        this.nodeId = nodeInfo.getNodeId();

        try {
            FileHandler fileHandler = new FileHandler("node_" + nodeId + ".log", true);

            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void becomeCoordinator() throws RemoteException {
        logger.info("Node " + nodeId + ": becomes a Leader");
        setCandidate(false);
        setCoordinator(nodeInfo);
        notifyAll(node -> {
            try {
                node.setCoordinator(nodeInfo);
            } catch (RemoteException ignored) {
            }
        });
        setElectionStarted(false);
    }

    @Override
    public void startElection() throws RemoteException {
        if (electionStarted) {
            logger.info("Node " + nodeId + ": election already started, skipping.");
            return;
        }
        setCandidate(true);
        setElectionStarted(true);
        Node sender = this;

        notifyAll(new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                try {
                    if (Integer.parseInt(node.getNodeId()) > Integer.parseInt(nodeId)) {
                        node.wakeUpElection(sender);
                    }
                } catch (RemoteException e) {
                    logger.severe("Node " + nodeId + ": Error during notifying nodes election");
                }
            }
        });

        if (isCandidate) {
            becomeCoordinator();
            return;
        }
    }

    @Override
    public void wakeUpElection(Node sender) throws RemoteException {
        sender.receiveLostStatus();
        logger.info("Node " + nodeId + ": pushes lost status to sender: " + sender.getNodeId());
        logger.info("Node " + nodeId + ": wakes up election");
        startElection();
    }

    @Override
    public void notifyAll(Consumer<Node> callback) throws RemoteException {
        ArrayList<NodeInfo> toRemove = new ArrayList<>();
        for (NodeInfo nodeInfo : neighbors) {
            if (nodeId.equals(nodeInfo.getNodeId())) {
                throw new RuntimeException("Implementation error! Node should not contain itself");
            }
            Node node = ServerRegistry.getNode(nodeInfo);
            if (node != null) {
                callback.accept(node);
            } else {
                toRemove.add(nodeInfo);
            }
        }
        neighbors.removeAll(toRemove);
    }

    @Override
    public Node getCoordinator() throws RemoteException {
        Node coordinator = ServerRegistry.getNode(nodeInfo.getCoordinator());
        if (coordinator == null) {
            logger.warning("Node " + nodeId + ": have null coordinator. Initializing coordinator");
            startElection();
            return getCoordinator();
        }

        return coordinator;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    @Override
    public void receiveLostStatus() throws RemoteException {
        setCandidate(false);
    }

    @Override
    public void addNeighbor(NodeInfo neighbour) throws RemoteException {
        if (!this.neighbors.contains(neighbour)) {
            neighbors.add(neighbour);
            Node node = ServerRegistry.getNode(neighbour);
            if (node == null) {
                logger.severe("Node " + neighbour.getNodeId() + ": Looks like not alive anymore");
                neighbors.remove(neighbour);
                return;
            }
            node.addNeighbor(nodeInfo);
        }
    }

    @Override
    public List<NodeInfo> getNeighbours() throws RemoteException {
        return neighbors;
    }

    public void setCandidate(boolean candidate) {
        isCandidate = candidate;
    }

    public void bindToServerWithNode(NodeInfo nodeInfoTo) throws RemoteException {
        String hostname = nodeInfo.getHostname();
        int port = nodeInfo.getPort();

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

        Node node = ServerRegistry.getNode(nodeInfoTo);
        assert node != null;
        for (NodeInfo neighbor : node.getNeighbours()) {
            this.addNeighbor(neighbor);
        }
        this.addNeighbor(nodeInfoTo);

        findCoordinator();
    }

    public void bindToServer() throws RemoteException {
        String hostname = nodeInfo.getHostname();
        int port = nodeInfo.getPort();

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

        becomeCoordinator();
    }

//    public void bindToNode(NodeInfo nodeInfoTo) throws RemoteException {
//        Node node = ServerRegistry.getNode(nodeInfoTo);
//        assert node != null;
//        for (NodeInfo neighbor : node.getNeighbours()) {
//            this.addNeighbor(neighbor);
//        }
//        this.addNeighbor(nodeInfoTo);
//        this.setCoordinator(node.getCoordinator().getNodeInfo());
//    }


    public void findCoordinator() throws RemoteException {
        Node coordinator = null;
        List<NodeInfo> toRemove = new ArrayList<>();
        for (NodeInfo neighbour : neighbors) {
            Node node = ServerRegistry.getNode(neighbour);
            if (node != null) {
                coordinator = node.getCoordinator();
                break;
            } else {
                toRemove.add(neighbour);
            }
        }
        neighbors.removeAll(toRemove);

        if (neighbors.isEmpty()) {
            logger.info("Node " + nodeId + ": no neighbors found. Becoming a leader");
            becomeCoordinator();
            return;
        }

        if (coordinator != null) {
            logger.info("Node " + nodeId + ": Coordinator is found. Coordinator id: " + coordinator.getNodeId());
            this.setCoordinator(coordinator.getNodeInfo());
        } else {
            logger.info("Node " + nodeId + ": Coordinator is not alive. Triggering election...");
            startElection();
        }
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
        isCoordinator = coordinator.equals(nodeInfo);
        if (!isCoordinator) setElectionStarted(false);
        nodeInfo.setCoordinator(coordinator);
    }

    @Override
    public List<NodeInfo> getNeighbors() {
        return neighbors;
    }

    @Override
    public String getFile() throws RemoteException {
        if (!isCoordinator) {
            return getCoordinator().getFile();
        }
        if (lock.isLocked()) {
            logger.info("Node (Leader) " + nodeId + ": file is occupated by someone. Waiting...");
        }
        return file;
    }

    @Override
    public String setFile(String file) throws RemoteException, InterruptedException, TimeoutException {
        if (!isCoordinator) {
            return getCoordinator().setFile(file);
        }

        int timeFromRequest = 0;
        while (!lock.tryLock()) {
            if (timeFromRequest++ > WAITING_LIMIT) {
                throw new TimeoutException();
            }
            logger.info("Node (Leader) " + nodeId + ": file is occupated by someone. Waiting...");
            Thread.sleep(2000);
        }
        this.file = file;
        notifyAll(new Consumer<Node>() {

            @Override
            public void accept(Node node) {
                try {
                    node.setFileByLeader(file);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        logger.info("Node (Leader) " + nodeId + ": file successfully updated everywhere, nice job!");
        lock.unlock();
        return file;
    }

    @Override
    public void setFileByLeader(String file) throws RemoteException {
        this.file = file;
        logger.info("Node " + nodeId + ": file is changed by LEADER");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NodeImpl node = (NodeImpl) o;
        return Objects.equals(nodeInfo, node.nodeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nodeInfo);
    }

    @Override
    public String toString() {
        return "NodeImpl{" +
                "nodeInfo=" + nodeInfo +
                ", nodeId='" + nodeId + '\'' +
                ", isCoordinator=" + isCoordinator +
                ", isCandidate=" + isCandidate +
                '}';
    }

    public void setElectionStarted(boolean electionStarted) throws RemoteException {
        this.electionStarted = electionStarted;
    }

    public static void main(String[] args) throws RemoteException {
        String host = System.getProperty("host");
        int port = Integer.parseInt(System.getProperty("port", "-1"));
        String nodeId = System.getProperty("nodeId");

        String toHost = System.getProperty("toHost", null);
        int toPort = Integer.parseInt(System.getProperty("toPort", "-1"));
        String toNodeId = System.getProperty("toNodeId", null);

        NodeImpl node = null;

        if (host == null || port == -1 || nodeId == null) {
            System.err.println("Usage: java Main <host> <port> <nodeId> [toHost toPort toNodeId]");
            return;
        }

        System.setProperty("java.rmi.server.hostname", "0.0.0.0");
        node = new NodeImpl(new NodeInfo(host, port, nodeId));

        if (toHost == null || toPort == -1 || toNodeId == null) {
            System.out.println("Starting single node");
            node.bindToServer();
        } else {
            System.out.println("Binding node to existing: " + toHost + "-" + toPort + "-" + toNodeId);
            node.bindToServerWithNode(new NodeInfo(toHost, toPort, toNodeId));
        }

        RestController restController = new RestController(node);
        restController.run();
    }
}
