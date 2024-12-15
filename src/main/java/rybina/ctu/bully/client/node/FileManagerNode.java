package rybina.ctu.bully.client.node;

import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.Simulation;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileManagerNode extends Remote {
     String getContent(String fileName, NodeInfo sender) throws RemoteException, NotBoundException;
     String getContent(String fileName) throws RemoteException, NotBoundException;
     List<Simulation.FileInfo> getAvailableFiles() throws RemoteException;
}
