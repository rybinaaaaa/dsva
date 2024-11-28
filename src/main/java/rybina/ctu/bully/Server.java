package rybina.ctu.bully;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

import static rybina.ctu.bully.utils.ServerProperties.getHost;
import static rybina.ctu.bully.utils.ServerProperties.getPort;
import static rybina.ctu.bully.utils.ServerRegistry.bindServerRegistry;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void startServer() {
        try {
            System.setProperty("java.rmi.server.hostname", getHost());
            logger.info("Preparing server...");
            bindServerRegistry(LocateRegistry.createRegistry(getPort()));
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

    public static ServerRegistry getRegistry() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(getHost(), getPort());
        if (registry == null) {
            logger.severe("Server is not running!");
            return null;
        }
        return ServerRegistry.get(registry);
    }

    public static void main(String[] args) {
        startServer();
    }
}
