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
import java.util.function.Consumer;
import java.util.logging.Logger;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());

    private final NodeInfo nodeInfo;

    private final String nodeId;
    private NodeInfo coordinator = null;
    private boolean isCoordinator;
    private boolean isCandidate;
    private boolean electionStarted = false;

    private final List<NodeInfo> neighbors = new ArrayList<>();


    public NodeImpl(String hostname, int port, String nodeId) throws RemoteException, NotBoundException {
        this.nodeInfo = new NodeInfo(hostname, port, nodeId);

        this.nodeId = nodeId;
        this.isCoordinator = false;
        this.isCandidate = false;
//        this.bindToServer();
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void becomeCoordinator() throws RemoteException, NotBoundException {
        setIsCoordinator(true);
        setCandidate(false);
        setCoordinator(this.getNodeInfo());
        notifyAll(node -> {
            try {
                node.setIsCoordinator(false);
                node.setCoordinator(this.getNodeInfo());
            } catch (RemoteException ignored) {
            }
        });
    }

    @Override
    public void startElection() throws RemoteException, NotBoundException {
        logger.info("Node " + nodeId + " Starting election");
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
                    logger.severe("Error during notifying nodes election");
                }
            }
        });

        if (isCandidate) {
            logger.info("Node " + nodeId + " becomes a leader");
            becomeCoordinator();
        }

        electionStarted = false;
    }

    @Override
    public void wakeUpElection(Node sender) throws RemoteException, NotBoundException {
        if (electionStarted) return;
        this.electionStarted = true;
        if (Integer.parseInt(sender.getNodeId()) > Integer.parseInt(nodeId)) {
            sender.receiveLostStatus();
        }

        this.startElection();
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
        try {
            return ServerRegistry.getNode(coordinator);
        } catch (NotBoundException e) {
            logger.warning("Leader with id " + coordinator.getNodeId() + " not found. Reevaluatind leader...");
            this.startElection();
            return this.getCoordinator();
        }
    }

//    public void initCoordinator() throws RemoteException, NotBoundException {
//        if (coordinator == null) {
//            logger.info("The node coordinator is uninitialized. Binding the coordinator...");
//            if (neighbors.isEmpty()) {
//                logger.info("The node is first in registry. Initialized node with id " + nodeId + " as coordinator.");
//                becomeCoordinator();
//                return;
//            }
//
//            logger.info("Getting coordinator from existing nodes");
//            for (NodeInfo nodeInfo : neighbors) {
//                if (nodeInfo.getNodeId().equals(String.valueOf(nodeId))) continue;
//
//                try {
//                    Node node = ServerRegistry.getNode(nodeInfo);
//
////                    The getCoordinator is null-safe
//                    Node coordinator = node.getCoordinator();
//                    setCoordinator(coordinator.getNodeInfo());
//                } catch (NotBoundException ignored) {
//                }
//            }
//        }
//    }

    @Override
    public boolean isCoordinator() {
        return isCoordinator;
    }

    @Override
    public void receiveLostStatus() throws RemoteException {
        setIsCoordinator(false);
    }

    @Override
    public void addNeighbor(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
            this.neighbors.add(nodeInfo);
    }

    @Override
    public void setIsCoordinator(boolean isCoordinator) throws RemoteException {
        if (isCoordinator) {
            coordinator = this.getNodeInfo();
            this.isCoordinator = true;
        } else {
            this.isCoordinator = false;
        }
    }

    public void setCandidate(boolean candidate) {
        isCandidate = candidate;
    }

    public void bindToServer() throws RemoteException, NotBoundException {
        String hostname = this.nodeInfo.getHostname();
        int port = this.nodeInfo.getPort();

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
        logger.info("Server ready at " + hostname + ":" + this.nodeInfo.getPort());
        registry.rebind(nodeId, this);
    }

    @Override
    public void showAllNodes() throws RemoteException {
        System.out.println("Node with id: " + getNodeId() + " has neighbours:");
        this.neighbors.forEach(System.out::println);
    }

    @Override
    public NodeInfo getNodeInfo() throws RemoteException {
        return nodeInfo;
    }

    public void setCoordinator(NodeInfo coordinator) throws RemoteException {
        this.isCoordinator = coordinator == nodeInfo;
        this.coordinator = coordinator;
    }

    public void bindNode(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
        this.addNeighbor(nodeInfo);
        ServerRegistry.getNode(nodeInfo).addNeighbor(this.getNodeInfo());
    }

    public void bindToNode(NodeImpl node) throws NotBoundException, RemoteException {
        for (NodeInfo neighbor : node.neighbors) {
            this.bindNode(neighbor);
        }

        if (node.coordinator == null) {
            startElection();
        } else {
            this.setCoordinator(node.coordinator);
        }
    }
}
