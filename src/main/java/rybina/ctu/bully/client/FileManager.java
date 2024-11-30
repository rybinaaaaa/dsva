package rybina.ctu.bully.client;

import rybina.ctu.bully.client.node.Node;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FileManager implements Serializable {
    private final Node node;
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());
    private final Map<String, StringBuilder> fileSimulation = new HashMap<>();

    public FileManager(Node node) {
        this.node = node;
    }

    public boolean createFile(String filename, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating createFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().createFile(filename, false);
        }

        if (fileSimulation.containsKey(filename)) {
            logger.warning("File already exists: " + filename);
            return false;
        }

        fileSimulation.put(filename, new StringBuilder());
        logger.info("File was successfully created: " + filename);

        if (!isCalledByCoord) {
            logger.info("Actualize files in the others nodes");
            node.notifyAll(n -> {
                try {
                    n.getFileManager().createFile(filename, true);
                } catch (RemoteException ignored) {
                }
            });
        }

        return true;
    }

    public boolean writeToFile(String filename, String content, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating writeToFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().writeToFile(filename, content, false);
        }

        if (!fileSimulation.containsKey(filename)) {
            logger.warning("File does not exist: " + filename);
            return false;
        }

        fileSimulation.get(filename).append(content).append("\n");
        logger.info("Data was successfully written to file: " + filename);

        if (!isCalledByCoord) {
            logger.info("Actualize files in the others nodes");
            node.notifyAll(n -> {
                try {
                    n.getFileManager().writeToFile(filename, content, true);
                } catch (RemoteException ignored) {
                }
            });
        }

        return true;
    }

    public void readFromFile(String filename, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating readFromFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            node.getCoordinator().getFileManager().readFromFile(filename, false);
            return;
        }

        if (!fileSimulation.containsKey(filename)) {
            logger.warning("File does not exist: " + filename);
            return;
        }

        String content = fileSimulation.get(filename).toString();
        logger.info("Read from file: " + content);
    }

    public boolean deleteFile(String filename, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating deleteFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().deleteFile(filename, false);
        }

        if (!fileSimulation.containsKey(filename)) {
            logger.warning("File does not exist: " + filename);
            return false;
        }

        fileSimulation.remove(filename);
        logger.info("File was successfully deleted: " + filename);

        if (!isCalledByCoord) {
            logger.info("Actualize files in the others nodes");
            node.notifyAll(n -> {
                try {
                    n.getFileManager().deleteFile(filename, true);
                } catch (RemoteException ignored) {
                }
            });
        }

        return true;
    }

    public boolean createFile(String filename) throws RemoteException {
        return createFile(filename, false);
    }

    public boolean writeToFile(String filename, String content) throws RemoteException {
        return writeToFile(filename, content, false);
    }

    public void readFromFile(String filename) throws RemoteException {
        readFromFile(filename, false);
    }

    public boolean deleteFile(String filename) throws RemoteException {
        return deleteFile(filename, false);
    }
}
