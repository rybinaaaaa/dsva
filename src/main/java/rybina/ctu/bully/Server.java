package rybina.ctu.bully;

import rybina.ctu.bully.client.node.NodeImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void startServer() {
        System.setProperty("java.rmi.server.hostname", "localhost");

        try {
            // Create nodes
            NodeImpl node1 = new NodeImpl("localhost", 1099, "1");
            NodeImpl node2 = new NodeImpl("localhost", 1099, "2");
            NodeImpl node3 = new NodeImpl("localhost", 1099, "3");


            node1.bindToServer();
            node2.bindToServer();
            node3.bindToServer();

            node2.bindToNode(node1);

        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}
