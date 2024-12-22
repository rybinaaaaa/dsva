package rybina.ctu.bully.client.node;

import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());

    private final NodeInfo nodeInfo;

    private final String nodeId;
    private boolean isCoordinator = false;
    private boolean isCandidate = false;
    private boolean electionStarted = false;

    private final List<NodeInfo> neighbors = new ArrayList<>();


    public NodeImpl(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
        this.nodeInfo = nodeInfo;
        this.nodeId = nodeInfo.getNodeId();
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void becomeCoordinator() throws RemoteException, NotBoundException {
        setCandidate(false);
        setCoordinator(nodeInfo);
        notifyAll(node -> {
            try {
                node.setCoordinator(nodeInfo);
            } catch (RemoteException ignored) {
            }
        });
    }

    @Override
    public void startElection() throws RemoteException, NotBoundException {
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
        if (Integer.parseInt(sender.getNodeId()) < Integer.parseInt(nodeId)) {
            sender.receiveLostStatus();
            logger.info("Node " + nodeId + ": pushes lost status to sender: " + sender.getNodeId());
        }

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
    public Node getCoordinator() throws RemoteException, NotBoundException {
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

    public void setCandidate(boolean candidate) {
        isCandidate = candidate;
    }

    public void bindToServer(List<NodeInfo> nodePool) throws RemoteException, NotBoundException {
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

        for (NodeInfo neighbour : nodePool) {
            addNeighbor(neighbour);
        }

        findCoordinator();
    }

    public void findCoordinator() throws RemoteException, NotBoundException {
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
        nodeInfo.setCoordinator(coordinator);
    }

//    public void bindNode(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
//        addNeighbor(nodeInfo);
//        ServerRegistry.getNode(nodeInfo).addNeighbor(getNodeInfo());
//    }

//    public void bindToNode(NodeImpl node) throws NotBoundException, RemoteException {
//        logger.info("Binding to node with id: " + node.getNodeId() + " node with id " + nodeId);
//
//        for (NodeInfo neighbor : node.getNeighbors()) {
//            bindNode(neighbor);
//        }
//
//        bindNode(node.getNodeInfo());
//        if (node.coordinator == null) {
//            startElection();
//        } else {
//            setCoordinator(node.coordinator);
//        }
//    }

    @Override
    public List<NodeInfo> getNeighbors() {
        return neighbors;
    }

    @Override
    public String getContent() throws RemoteException, NotBoundException {
        return "";
    }

    @Override
    public String setContent() throws RemoteException {
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NodeImpl node = (NodeImpl) o;
        return Objects.equals(nodeId, node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nodeId);
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
}
