package rybina.ctu.bully.client;

import rybina.ctu.bully.client.node.Node;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeBinder extends Remote {

     Node bindNode(Integer nodeId) throws RemoteException, NotBoundException;
}
