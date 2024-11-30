package rybina.ctu.bully;
import rybina.ctu.bully.client.NodeBinder;
import rybina.ctu.bully.client.NodeBinderImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

import static rybina.ctu.bully.utils.ServerProperties.getHost;
import static rybina.ctu.bully.utils.ServerProperties.getPort;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void startServer() {
        try {
            System.setProperty("java.rmi.server.hostname", getHost());
            logger.info("Preparing server...");
            Registry registry = LocateRegistry.createRegistry(getPort());
            registry.rebind(NodeBinder.class.getName(), new NodeBinderImpl());
            logger.info("Server ready!");

            while (true) {
                Thread.sleep(1000000);
            }
        } catch (RemoteException e) {
            logger.severe("Server exception: " + e);
        } catch (InterruptedException e) {
            logger.severe("Server has been interrupted: " + e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}
