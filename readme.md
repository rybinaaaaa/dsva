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

The `setFile` method is used to update the file content managed by the node. If the node is the coordinator, it queues the file update request for processing. If the node is not the coordinator, it delegates the file update to the coordinator.

**Steps:**

1. If the node is not the coordinator, it calls the `setFile` method on the coordinator.
2. If the node is the coordinator:
   - It queues the file update request in the `BlockingQueue` named `fileUpdateQueue`.

```java
@Override
public String setFile(String file) throws RemoteException {
    if (!isCoordinator) {
        return getCoordinator().setFile(file);
    }
    try {
        fileUpdateQueue.put(file);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RemoteException("Failed to queue file update request", e);
    }
    return file;
}
```

#### Queue Logic for File Updates

The `NodeImpl` class uses a `BlockingQueue` to manage file update requests. This ensures that file updates are processed in the order they are received, preventing race conditions and ensuring consistency.

**How It Works:**

1. **Initialization**:
   - A `BlockingQueue` named `fileUpdateQueue` is initialized to hold file update requests.

2. **Worker Thread**:
   - A worker thread is started in the constructor of `NodeImpl` to process file update requests. This thread runs the `processFileUpdates` method.

3. **Processing File Updates**:
   - The `processFileUpdates` method continuously takes file update requests from the `fileUpdateQueue` and processes them using the `updateFile` method.
   - The `updateFile` method acquires a lock on the file, updates the file content, and notifies all neighbors of the change.

```java
private void processFileUpdates() {
    logger.info("Node " + nodeId + ": Starting processFileUpdates thread.");
    while (true) {
        try {
            String newFileContent = fileUpdateQueue.take();
            logger.info("Node " + nodeId + ": Processing file update: " + newFileContent);
            updateFile(newFileContent);
        } catch (InterruptedException e) {
            logger.warning("Node " + nodeId + ": processFileUpdates thread interrupted.");
            Thread.currentThread().interrupt();
            break;
        } catch (RemoteException e) {
            logger.severe("Node " + nodeId + ": RemoteException in processFileUpdates: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            logger.severe("Node " + nodeId + ": TimeoutException in processFileUpdates: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    logger.info("Node " + nodeId + ": Exiting processFileUpdates thread.");
}

private void updateFile(String newFileContent) throws InterruptedException, TimeoutException, RemoteException {
    int timeFromRequest = 0;
    while (!lock.tryLock()) {
        if (timeFromRequest++ > WAITING_LIMIT) {
            throw new TimeoutException();
        }
        logger.info("Node (Leader) " + nodeId + ": file is occupied by someone. Waiting...");
        Thread.sleep(2000);
    }
    logger.info("Node (Leader) " + nodeId + ": Acquired lock, updating file to: " + newFileContent);
    this.file = newFileContent;
    notifyAll(node -> {
        try {
            node.setFileByLeader(newFileContent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    });
    Thread.sleep(2000); // Simulate long operation
    logger.info("Node (Leader) " + nodeId + ": file successfully updated everywhere, nice job!");
    lock.unlock();
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

### REST API

The application provides a REST API for interacting with the nodes. Below are the available endpoints and their usage.

#### Endpoints

- `GET /<nodeId>`: Get node information
- `POST /<nodeId>/add_node`: Add a new node
- `GET /<nodeId>/neighbours`: Get neighbors information
- `POST /<nodeId>/kill`: Kill the node
- `POST /<nodeId>/file/set`: Set the file content
- `GET /<nodeId>/file/get`: Get the file content

#### Example Usage

1. **Get Node Information**

```bash
curl -X GET http://<host>:<port>/<nodeId>
```

2. **Add a New Node**

```bash
curl -X POST http://<host>:<port>/<nodeId>/add_node -H "Content-Type: application/json" -d '{"hostname": "<hostname>", "port": <port>, "nodeId": "<newNodeId>"}'
```

3. **Get Neighbors Information**

```bash
curl -X GET http://<host>:<port>/<nodeId>/neighbours
```

4. **Kill the Node**

```bash
curl -X POST http://<host>:<port>/<nodeId>/kill
```

5. **Set the File Content**

```bash
curl -X POST http://<host>:<port>/<nodeId>/file/set -H "Content-Type: text/plain" -d '<fileContent>'
```

6. **Get the File Content**

```bash
curl -X GET http://<host>:<port>/<nodeId>/file/get
```

Replace `<host>`, `<port>`, `<nodeId>`, `<hostname>`, `<newNodeId>`, and `<fileContent>` with the appropriate values for your setup.