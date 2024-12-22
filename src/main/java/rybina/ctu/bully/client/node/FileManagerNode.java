package rybina.ctu.bully.client.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileManagerNode extends Remote {
     String getContent() throws RemoteException;
     String setContent() throws RemoteException;
}
