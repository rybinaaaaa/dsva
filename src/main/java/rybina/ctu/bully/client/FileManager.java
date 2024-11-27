package rybina.ctu.bully.client;

import rybina.ctu.bully.client.node.NodeImpl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.logging.*;

public class FileManager {
    private final NodeImpl node;
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());

    public FileManager(NodeImpl node) {
        this.node = node;
    }

    public boolean createFile(String filename, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating createFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().createFile(filename, false);
        }

        File file = new File(filename);
        try {
            if (file.createNewFile()) {
                logger.info("File was successfully created: " + filename);
                if (!isCalledByCoord) {
                    logger.info("Actualize files in the others nodes");
                    node.notifyAll(n -> {
                        try {
                            n.getFileManager().createFile(filename,true);
                        } catch (RemoteException ignored) {
                        }
                    });
                }
                return true;
            } else {
                logger.warning("File already exists: " + filename);
                return false;
            }
        } catch (IOException e) {
            logger.severe("Error occurred while creating file: " + e.getMessage());
            return false;
        }
    }

    public boolean writeToFile(String filename, String content, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating writeToFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().writeToFile(filename, content, false);
        }

        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(content + "\n");
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
        } catch (IOException e) {
            logger.severe("Error occurred while writing to file: " + e.getMessage());
            return false;
        }
    }

    public void readFromFile(String filename, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating readFromFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            node.getCoordinator().getFileManager().readFromFile(filename, false);
            return;
        }

        File file = new File(filename);
        if (!file.exists()) {
            logger.warning("File does not exist: " + filename);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("Read from file: " + line);
            }
        } catch (IOException e) {
            logger.severe("Error occurred while reading file: " + e.getMessage());
        }
    }

    public boolean deleteFile(String filename, boolean isCalledByCoord) throws RemoteException {
        if (!node.isCoordinator() && !isCalledByCoord) {
            logger.info("Delegating deleteFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().deleteFile(filename, false);
        }

        File file = new File(filename);
        if (file.exists()) {
            if (file.delete()) {
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
            } else {
                logger.severe("Failed to delete file: " + filename);
                return false;
            }
        } else {
            logger.warning("File does not exist: " + filename);
            return false;
        }
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