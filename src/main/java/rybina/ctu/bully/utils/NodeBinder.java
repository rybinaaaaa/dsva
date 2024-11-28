package rybina.ctu.bully.utils;

import rybina.ctu.bully.client.node.Node;
import rybina.ctu.bully.client.node.NodeImpl;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeBinder extends Remote {

     Node bindNode(Integer nodeId) throws RemoteException, NotBoundException;
}
