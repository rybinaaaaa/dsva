### Documentation Nodes in Bully Algorithm

The `NodeImpl` class implements the Bully algorithm for leader election in a distributed system. The Bully algorithm is
used to elect a coordinator (leader) among distributed nodes. Below is an explanation of how the `NodeImpl` class works
and how it implements the Bully algorithm.

#### Class Overview

The `NodeImpl` class extends `UnicastRemoteObject` and implements the `Node` interface. It represents a node in a
distributed system that can participate in leader election using the Bully algorithm and a skeleton from RMI point of
view.

#### Key Fields

- `nodeInfo`: Contains information about the node, such as hostname, port, and node ID.
- `lock`: A `ReentrantLock` used to synchronize access to the file.
- `file`: A string representing the file content managed by the node.
- `WAITING_LIMIT`: The maximum time a node waits for the file to be available.
- `nodeId`: The unique identifier of the node.
- `isCoordinator`: A boolean indicating if the node is the coordinator.
- `isCandidate`: A boolean indicating if the node is a candidate in the election.
- `electionStarted`: A boolean indicating if an election has started.
- `neighbors`: A list of neighboring nodes.

#### Key Methods

- `NodeImpl(NodeInfo nodeInfo)`: Constructor that initializes the node with the given `NodeInfo`.
- `getNodeId()`: Returns the node's ID.
- `becomeCoordinator()`: Makes the node the coordinator and notifies all neighbors.
- `startElection()`: Starts the election process.
- `wakeUpElection(Node sender)`: Wakes up the node to participate in the election.
- `notifyAll(Consumer<Node> callback)`: Notifies all neighbors with the given callback.
- `getCoordinator()`: Returns the current coordinator.
- `receiveLostStatus()`: Marks the node as not a candidate - it's lost.
- `addNeighbor(NodeInfo neighbor)`: Adds a neighbor to the node.
- `getNeighbours()`: Returns the list of neighbors.
- `bindToServerWithNode(NodeInfo nodeInfoTo)`: Binds the node to an existing node in the network.
- `bindToServer()`: Binds the node to the server and makes it the coordinator.
- `findCoordinator()`: Finds the current coordinator among the neighbors.
- `showAllNeighbours()`: Prints all neighbors of the node.
- `getNodeInfo()`: Returns the node's information.
- `setCoordinator(NodeInfo coordinator)`: Sets the coordinator for the node.
- `getFile()`: Returns the file content.
- `setFile(String file)`: Sets the file content and notifies all neighbors.
- `setFileByLeader(String file)`: Sets the file content as instructed by the leader.
- `isUniqueId(String nodeId)`: Checks if the given node ID is unique among the neighbors.

#### Bully Algorithm Implementation

1. **Election Start**: When a node detects that the coordinator is not responding, it starts an election by calling
   `startElection()`.
2. **Election Process**:
   The node sends election messages to all nodes with higher IDs. If no higher ID nodes respond, the node becomes the
   coordinator by calling `becomeCoordinator()`.
   If the node has greater id and it's alive - it pushes **LOST** status to the sender and starts its own election.
3. **Coordinator Announcement**: The new coordinator notifies all nodes about its new role by calling `notifyAll()`.
4. **Handling Election Messages**: When a node receives an election message from a lower ID node, it responds and starts
   its own election if it is not already a candidate.
5. **Coordinator Lookup**: Nodes can find the current coordinator by calling `getCoordinator()`, which triggers an
   election if the coordinator is not found.

#### Reasons for starting Election:

1. The system starts and doesn't have a coordinator.
2. The system notices that the coordinator is not unreachable.

#### How the node is Running:

```java
public static void main(String[] args) throws RemoteException {
    String host = System.getProperty("host");
    int port = Integer.parseInt(System.getProperty("port", "-1"));
    String nodeId = System.getProperty("nodeId");

    String toHost = System.getProperty("toHost", null);
    int toPort = Integer.parseInt(System.getProperty("toPort", "-1"));
    String toNodeId = System.getProperty("toNodeId", null);

    if (host == null || port == -1 || nodeId == null) {
        System.err.println("Usage: java Main <host> <port> <nodeId> [toHost toPort toNodeId]");
        return;
    }

    System.setProperty("java.rmi.server.hostname", host);
    NodeImpl node = new NodeImpl(new NodeInfo(host, port, nodeId));

    if (toHost == null || toPort == -1 || toNodeId == null) {
        System.out.println("Starting single node");
        node.bindToServer();
    } else {
        System.out.println("Binding node to existing: " + toHost + "-" + toPort + "-" + toNodeId);
        node.bindToServerWithNode(new NodeInfo(toHost, toPort, toNodeId));
    }

    RestController restController = new RestController(node);
    restController.run();
}
```

Requred input arguments are `host`, `port`, and `nodeId`. Optional arguments are `toHost`, `toPort`, and `toNodeId`.
If the optional arguments are provided, the node will bind to an existing node in the network.
Otherwise, the node will start as a single node and become the coordinator.
java -Dhost="_HOST_" -Dport="_PORT_" -DnodeId="_NODE_ID_"
Optional:
-DtoHost="_TO_HOST_" -DtoPort="_TO_PORT_" -DtoNodeId="_TO_NODE_ID_"

### `setFile` and `getFile` Methods

#### `setFile` Method

The `setFile` method is used to update the file content managed by the node. If the node is the coordinator, it locks
the file, updates its content, and notifies all neighbors about the change. If the node is not the coordinator, it
delegates the file update to the coordinator.

**Steps:**

1. If the node is not the coordinator, it calls the `setFile` method on the coordinator.
2. If the node is the coordinator:
    - It tries to acquire the lock on the file.
    - If the lock is acquired, it updates the file content.
    - It notifies all neighbors about the file update by calling their `setFileByLeader` method.
    - It releases the lock after the update.

```java

@Override
public String setFile(String file) throws RemoteException, InterruptedException, TimeoutException {
    if (!isCoordinator) {
        return getCoordinator().setFile(file);
    }

    int timeFromRequest = 0;
    while (!lock.tryLock()) {
        if (timeFromRequest++ > WAITING_LIMIT) {
            throw new TimeoutException();
        }
        logger.info("Node (Leader) " + nodeId + ": file is occupied by someone. Waiting...");
        Thread.sleep(2000);
    }
    this.file = file;
    notifyAll(node -> {
        try {
            node.setFileByLeader(file);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    });
    Thread.sleep(2000); // Simulate long operation
    logger.info("Node (Leader) " + nodeId + ": file successfully updated everywhere, nice job!");
    lock.unlock();
    return file;
}
```

#### `getFile` Method

The `getFile` method is used to retrieve the file content managed by the node. If the node is the coordinator, it
returns the file content directly. If the node is not the coordinator, it delegates the file retrieval to the
coordinator.

**Steps:**

1. If the node is not the coordinator, it calls the `getFile` method on the coordinator.
2. If the node is the coordinator:
    - It checks if the file is locked and logs a message if it is.
    - It returns the file content.

```java

@Override
public String getFile() throws RemoteException {
    if (!isCoordinator) {
        return getCoordinator().getFile();
    }
    if (lock.isLocked()) {
        logger.info("Node (Leader) " + nodeId + ": file is occupied by someone. Waiting...");
    }
    return file;
}
```