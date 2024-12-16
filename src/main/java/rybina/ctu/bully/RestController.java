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
                // Читаем параметры из пути
                String hostname = ctx.pathParam("hostname");
                int port = Integer.parseInt(ctx.pathParam("port"));
                String id = ctx.pathParam("id");

                // Ищем узел
                Node node = ServerRegistry.getNode(hostname, port, id);

                // Если узел не найден, возвращаем 404
                if (node == null) {
                    ctx.status(404).result("Node not found");
                } else {
                    // Если найден, возвращаем в формате JSON
                    System.out.println(node.getNodeInfo());
                    ctx.json(node.getNodeInfo());
                }
            } catch (NumberFormatException e) {
                // Если port некорректен, возвращаем 400 Bad Request
                ctx.status(400).result("Invalid port number");
            } catch (Exception e) {
                // Любые другие ошибки возвращают 500 Internal Server Error
                ctx.status(500).result("An error occurred: " + e.getMessage());
            }
        });
    }
}
