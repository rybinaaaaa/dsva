package rybina.ctu.bully.client.node;

import rybina.ctu.bully.utils.NodeInfo;
import rybina.ctu.bully.utils.Simulation;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileManagerNode extends Remote {
     String getContent() throws RemoteException, NotBoundException;
     String setContent() throws RemoteException;
}
