package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.function.Consumer;

public class Consumers {
    public static class CreateFileConsumer implements Consumer<Node>, Serializable {
        private final String filename;

        public CreateFileConsumer(String filename) {
            this.filename = filename;
        }

        @Override
        public void accept(Node n) {
            try {
                n.getFileManager().createFile(filename, true);
            } catch (RemoteException ignored) {
            }
        }
    }

    public static class DeleteFileConsumer implements Consumer<Node>, Serializable {
        private final String filename;

        public DeleteFileConsumer(String filename) {
            this.filename = filename;
        }

        @Override
        public void accept(Node n) {
            try {
                n.getFileManager().deleteFile(filename, true);
            } catch (RemoteException ignored) {
            }
        }
    }

    public static class WriteFileConsumer implements Consumer<Node>, Serializable {
        private final String filename;
        private final String content;

        public WriteFileConsumer(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }

        @Override
        public void accept(Node n) {
            try {
                n.getFileManager().writeToFile(filename, content, true);
            } catch (RemoteException ignored) {
            }
        }
    }

}
