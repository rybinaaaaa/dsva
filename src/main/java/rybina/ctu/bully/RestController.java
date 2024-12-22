package rybina.ctu.bully;

import io.javalin.Javalin;
import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.client.node.NodeImpl;
import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class RestController {

    public static List<NodeInfo> nodes = new ArrayList<>();

    public static void main(String[] args) {
        Javalin app = Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);

        app.post("/add_node", ctx -> {
            NodeInfo nodeInfo = ctx.bodyAsClass(NodeInfo.class);
            NodeImpl node = new NodeImpl(nodeInfo);
            try {
                node.bindToServer(nodes);
                nodes.add(node.getNodeInfo());
                System.out.println("Node is added. " + node.getNodeInfo());
                ctx.status(200);
            } catch (RemoteException e) {
                System.out.println("Some Exception: " + e.getMessage());
                ctx.status(404);
            }
        });

        app.get("/node/{hostname}/{port}/{id}", ctx -> {
            try {
                String hostname = ctx.pathParam("hostname");
                int port = Integer.parseInt(ctx.pathParam("port"));
                String id = ctx.pathParam("id");

                Node node = ServerRegistry.getNode(hostname, port, id);

                if (node == null) {
                    ctx.status(404).result("Node not found");
                } else {
                    System.out.println(node.getNodeInfo());
                    ctx.json(node.getNodeInfo());
                }
            } catch (NumberFormatException e) {
                ctx.status(400).result("Invalid port number");
            } catch (Exception e) {
                ctx.status(500).result("An error occurred: " + e.getMessage());
            }
        });

        // New Endpoints

        // Endpoint to get all neighbors of a node
        app.get("/node/{hostname}/{port}/{id}/neighbors", ctx -> {
            try {
                String hostname = ctx.pathParam("hostname");
                int port = Integer.parseInt(ctx.pathParam("port"));
                String id = ctx.pathParam("id");

                Node node = ServerRegistry.getNode(hostname, port, id);

                if (node == null) {
                    ctx.status(404).result("Node not found");
                } else {
                    ctx.json(node.getNeighbors());
                }
            } catch (Exception e) {
                ctx.status(500).result("An error occurred: " + e.getMessage());
            }
        });

        app.get("/node/{hostname}/{port}/{id}/coordinator", ctx -> {
            try {
                String hostname = ctx.pathParam("hostname");
                int port = Integer.parseInt(ctx.pathParam("port"));
                String id = ctx.pathParam("id");

                Node node = ServerRegistry.getNode(hostname, port, id);

                if (node == null) {
                    ctx.status(404).result("Node not found");
                } else {
                    ctx.json(node.getCoordinator().getNodeInfo());
                }
            } catch (Exception e) {
                ctx.status(500).result("An error occurred: " + e.getMessage());
            }
        });

        app.post("/node/{hostname}/{port}/{id}/remove", ctx -> {
            String hostname = ctx.pathParam("hostname");
            int port = Integer.parseInt(ctx.pathParam("port"));
            String id = ctx.pathParam("id");

            if (ServerRegistry.removeNode(hostname, port, id)) {
                ctx.result("Node removed successfully.");
                System.out.println("Node with id: " + id +" is removed");
            } else {
                ctx.status(404).result("Node not found");
            }
        });
    }
}
