package rybina.ctu.bully;

import rybina.ctu.bully.client.node.NodeImpl;
import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.Simulation;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Server {
    public static void startServer() {
        System.setProperty("java.rmi.server.hostname", "localhost");

        try {
            // Create nodes
            NodeImpl node1 = new NodeImpl(new NodeInfo("localhost", 1099, "1", Simulation.PermissionRole.GUEST));
            NodeImpl node2 = new NodeImpl(new NodeInfo("localhost", 1097, "2", Simulation.PermissionRole.GUEST));
            NodeImpl node3 = new NodeImpl(new NodeInfo("localhost", 1096, "3", Simulation.PermissionRole.GUEST));

            node2.bindToNode(node1);
            node3.bindToNode(node1);

            System.out.println("Test on working nodes....");
            node1.showAllNeighbours();
            node2.showAllNeighbours();

            System.out.println("Test on accessing files...");
            ArrayList<Simulation.FileInfo> availableFiles = node3.getAvailableFiles();
            availableFiles.forEach(System.out::println);

            System.out.println(node3.getContent("music.mp3"));

        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}
