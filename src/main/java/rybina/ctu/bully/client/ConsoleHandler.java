package rybina.ctu.bully.client;

import rybina.ctu.bully.Server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class ConsoleHandler {
    private static final Logger logger = Logger.getLogger(ConsoleHandler.class.getName());

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("File Management Console");

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Add a file");
            System.out.println("2. Get a file");
            System.out.println("3. Delete a file");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please choose a valid option.");
            }
        }
    }

    public static void main(String[] args) throws RemoteException {
        ConsoleHandler consoleHandler = new ConsoleHandler();
//        if (args.length < 1) {
//            logger.severe("<NodeId> argument missing");
//            return;
//        }

        Server.startServer();
        Server.addNodeToRegistry(2);
        Server.addNodeToRegistry(3);
        Server.addNodeToRegistry(4);
        consoleHandler.start();
    }
}
