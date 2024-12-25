package rybina.ctu.bully;

import io.javalin.Javalin;
import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.client.node.NodeImpl;
import rybina.ctu.bully.utils.FileUpdateRequest;
import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RestController {

    public static List<NodeInfo> nodes = new ArrayList<>();

    public static void main(String[] args) {
        Javalin app = Javalin.create()
                .get("/", ctx -> {
                    updateNodes();
                    ctx.json(nodes);
                })
                .start(7070);

//        add node
        app.post("/add_node", ctx -> {
            NodeInfo nodeInfo = ctx.bodyAsClass(NodeInfo.class);
            NodeImpl node = new NodeImpl(nodeInfo);
            try {
                updateNodes();
                if (!isUniqueId(nodeInfo.getNodeId())) {
                    System.out.println("Node with id " + nodeInfo.getNodeId() + " already exists");
                    ctx.status(404);
                    return;
                }
                node.bindToServer(nodes);
                nodes.add(node.getNodeInfo());
                System.out.println("Node is added. " + node.getNodeInfo());
                ctx.status(200).json(nodeInfo);
            } catch (RemoteException e) {
                System.out.println("Some Exception: " + e.getMessage());
                ctx.status(404);
            }
        });

//        get info
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

        app.post("/node/remove", ctx -> {
            NodeInfo nodeInfo = ctx.bodyAsClass(NodeInfo.class);

            if (ServerRegistry.removeNode(nodeInfo)) {
                nodes.remove(nodeInfo);
                ctx.result("Node removed successfully.");
                System.out.println("Node with id: " + nodeInfo.getNodeId() + " is removed");
            } else {
                ctx.status(404).result("Node not found");
            }
        });

//        set file
        app.post("/node/{hostname}/{port}/{id}/file", ctx -> {
            try {
                FileUpdateRequest fileUpdateRequest = ctx.bodyAsClass(FileUpdateRequest.class);

                Node node = ServerRegistry.getNode(fileUpdateRequest.getNodeInfo());

                if (node == null) {
                    ctx.status(404).result("Node not found");
                    return;
                }

                String result = node.setFile(fileUpdateRequest.getFileContent());
                ctx.result("File updated successfully: " + result);
            } catch (RemoteException e) {
                ctx.status(500).result("Error during RMI operation: " + e.getMessage());
            } catch (InterruptedException | TimeoutException e) {
                ctx.status(500).result("Operation was interrupted: " + e.getMessage());
            } catch (Exception e) {
                ctx.status(500).result("An error occurred: " + e.getMessage());
            }
        });
    }

    public static void updateNodes() throws RemoteException {
        List<NodeInfo> toRemove = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = ServerRegistry.getNode(nodes.get(i));
            if (node == null) {
                toRemove.add(nodes.get(i));
            } else {
                nodes.set(i, node.getNodeInfo());
            }
        }
        nodes.removeAll(toRemove);
    }

    public static boolean isUniqueId(String id) {
        for (NodeInfo nodeInfo : nodes) {
            if (nodeInfo.getNodeId().equals(id)) {
                return false;
            }
        }
        return true;
    }
}
