package rybina.ctu.bully;

import io.javalin.Javalin;
import rybina.ctu.bully.client.node.NodeImpl;
import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.RemoteException;
import java.util.concurrent.TimeoutException;

public class RestController {

    public String pathToNode;
    private final NodeImpl node;

    public RestController(NodeImpl node) throws RemoteException {
        this.node = node;
        this.pathToNode = node.getNodeInfo().getNodeId();
    }

    public void run() throws RemoteException {

        Javalin app = Javalin.create()
                .get(pathToNode, ctx -> {
                    ctx.json(this.node.getNodeInfo());
                })
                .start(Integer.parseInt("1".concat(String.valueOf(node.getNodeInfo().getPort()))));

//        add node
        app.post(pathToNode.concat("/add_node"), ctx -> {
            NodeInfo nodeInfo = ctx.bodyAsClass(NodeInfo.class);
            NodeImpl newNode = new NodeImpl(nodeInfo);
            try {
                if (!isUniqueId(nodeInfo.getNodeId())) {
                    System.out.println("Node with id " + nodeInfo.getNodeId() + " already exists");
                    ctx.status(404);
                    return;
                }
                newNode.bindToServerWithNode(node.getNodeInfo());
                System.out.println("Node is added. " + node.getNodeInfo());
                ctx.status(200).json(nodeInfo);
            } catch (RemoteException e) {
                System.out.println("Some Exception: " + e.getMessage());
                ctx.status(404);
            }
        });

//        get neighbours info
        app.get(pathToNode.concat("/neighbours"), ctx -> {
            ctx.json(this.node.getNeighbors());
        });


//        kill
        app.post(pathToNode.concat("/kill"), ctx -> {

            if (ServerRegistry.removeNode(node.getNodeInfo())) {
                ctx.result("Node is killed.");
                System.out.println("Node with id: " + node.getNodeId() + " is killed");
                app.stop();
            } else {
                ctx.status(503).result("Something went wrong");
            }
        });

//        set file
        app.post(pathToNode.concat("/file/set"), ctx -> {
            try {
                String content = ctx.bodyAsClass(String.class);
                String result = node.setFile(content);
                ctx.result("File updated successfully: " + result);
            } catch (RemoteException e) {
                ctx.status(500).result("Error during RMI operation: " + e.getMessage());
            } catch (Exception e) {
                ctx.status(500).result("An error occurred: " + e.getMessage());
            }
        });

        //        get file
        app.get(pathToNode.concat("/file/get"), ctx -> {
            try {
                String result = node.getFile();
                System.out.println("Read file: " + result);
                ctx.result("File content: " + result);
            } catch (RemoteException e) {
                ctx.status(500).result("Error during RMI operation: " + e.getMessage());
            } catch (Exception e) {
                ctx.status(500).result("An error occurred: " + e.getMessage());
            }
        });
    }

    public boolean isUniqueId(String id) throws RemoteException {
        if (node.getNodeId().equals(id)) return false;
        for (NodeInfo nodeInfo : node.getNeighbours()) {
            if (nodeInfo.getNodeId().equals(id)) {
                return false;
            }
        }
        return true;
    }
}