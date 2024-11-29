package rybina.ctu.bully.client;

import rybina.ctu.bully.client.node.Node;

import java.io.*;
import java.rmi.RemoteException;
import java.util.logging.*;

public class FileManager implements Serializable {
    private final Node node;
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());

    public FileManager(Node node) {
        this.node = node;
    }

    public boolean createFile(String filename) throws RemoteException {
        if (!node.isCoordinator()) {
            logger.info("Delegating createFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().createFile(filename);
        }

        File file = new File(filename);
        try {
            if (file.createNewFile()) {
                logger.info("File was successfully created: " + filename);
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

    public boolean writeToFile(String filename, String content) throws RemoteException {
        if (!node.isCoordinator()) {
            logger.info("Delegating writeToFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().writeToFile(filename, content);
        }

        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(content + "\n");
            logger.info("Data was successfully written to file: " + filename);
            return true;
        } catch (IOException e) {
            logger.severe("Error occurred while writing to file: " + e.getMessage());
            return false;
        }
    }

    public void readFromFile(String filename) throws RemoteException {
        if (!node.isCoordinator()) {
            logger.info("Delegating readFromFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            node.getCoordinator().getFileManager().readFromFile(filename);
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

    public boolean deleteFile(String filename) throws RemoteException {
        if (!node.isCoordinator()) {
            logger.info("Delegating deleteFile request for '" + filename + "' from Node with id: " + node.getNodeId() + " to coordinator Node " + node.getCoordinator().getNodeId());
            return node.getCoordinator().getFileManager().deleteFile(filename);
        }

        File file = new File(filename);
        if (file.exists()) {
            if (file.delete()) {
                logger.info("File was successfully deleted: " + filename);
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
}