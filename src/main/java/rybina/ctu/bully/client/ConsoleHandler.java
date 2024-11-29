package rybina.ctu.bully.client;
import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.utils.ServerRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Logger;

public class ConsoleHandler {
    private static final Logger logger = Logger.getLogger(ConsoleHandler.class.getName());
    private final FileManager fileManager;
    private static final String CANCEL_OPTION = "6";

    public ConsoleHandler(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        String input;
        boolean exit = false;

        while (!exit) {
            System.out.println("FileManager Console - Available commands: ");
            System.out.println("1. create <filename>");
            System.out.println("2. write <filename> <content>");
            System.out.println("3. read <filename>");
            System.out.println("4. delete <filename>");
            System.out.println("5. exit");
            System.out.print("> ");
            input = scanner.nextLine();

            char option = input.charAt(0);

            try {
                switch (option) {
                    case '1':
                        System.out.println("Enter filename or '6' to exit...");
                        System.out.print("> ");
                        input = scanner.nextLine();
                        if (!Objects.equals(CANCEL_OPTION, input)) {
                            fileManager.createFile(input);
                        }
                        break;
                    case '2':
                        System.out.println("Enter filename or '6' to exit...");
                        System.out.print("> ");
                        input = scanner.nextLine();
                        if (!Objects.equals(CANCEL_OPTION, input)) {
                            String filename = input;

                            System.out.println("Enter content or '6' to exit...");
                            System.out.print("> ");
                            input = scanner.nextLine();
                            if (!Objects.equals(CANCEL_OPTION, input)) {
                                fileManager.writeToFile(filename, input);
                            }
                        }
                        break;
                    case '3':
                        System.out.println("Enter filename to read or '6' to exit...");
                        System.out.print("> ");
                        input = scanner.nextLine();
                        if (!Objects.equals(CANCEL_OPTION, input)) {
                            fileManager.readFromFile(input);
                        }
                        break;
                    case '4':
                        System.out.println("Enter filename to delete or '6' to exit...");
                        System.out.print("> ");
                        input = scanner.nextLine();
                        if (!Objects.equals(CANCEL_OPTION, input)) {
                            fileManager.deleteFile(input);
                        }
                        break;
                    case '5':
                        exit = true;
                        break;
                    default:
                        System.out.println("Unknown command. Try again.");
                }
            } catch (RemoteException e) {
                logger.severe("Error occurred while processing request: " + e.getMessage());
            }
        }
        scanner.close();
    }

    public static void main(String[] args) throws RemoteException, NotBoundException {
        if (args.length != 1) {
            logger.severe("Usage: ConsoleHandler <nodeId>");
        }

        Node node = ServerRegistry.addNodeToRegistry(Integer.parseInt(args[0]));

        ConsoleHandler consoleHandler = new ConsoleHandler(node.getFileManager());
        consoleHandler.start();
    }
}
