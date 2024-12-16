package rybina.ctu.bully;

import io.javalin.Javalin;
import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.client.node.NodeImpl;
import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.RemoteException;

public class RestController {

    private static NodeImpl root = null;

    public static void main(String[] args) {
        Javalin app = Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);

        app.post("/add_node", ctx -> {
            NodeInfo nodeInfo = ctx.bodyAsClass(NodeInfo.class);
            NodeImpl node = new NodeImpl(nodeInfo);
            try {
                node.bindToServer();
                if(root == null) {
                    root = node;
                } else {
                    node.bindToNode(root);
                }
                System.out.println("Node is added. " + node.getNodeInfo().toString());
                ctx.status(200);
            } catch (RemoteException e) {
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
    }
}
