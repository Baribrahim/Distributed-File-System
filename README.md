# Distributed File System

## Overview
This project is a **Distributed File Storage System** developed as part of a university coursework. The system supports multiple concurrent clients who can store, load, list, and remove files across distributed storage nodes (Dstores). The design ensures scalability, fault tolerance, and efficient file replication across nodes using **Java** and **TCP networking**.

## Key Features
- **Distributed Architecture**: The system consists of a Controller and multiple Data Stores (Dstores) that communicate via TCP connections.
- **File Replication**: Files are replicated across multiple Dstores to ensure data redundancy and availability.
- **Concurrency Handling**: Multiple clients can send concurrent requests to store, load, and remove files.
- **Failure Handling**: Dstores can fail without affecting the overall functionality, and files are redistributed as needed.

## Technologies Used
- **Programming Language**: Java
- **Networking**: TCP/IP communication between Controller, Clients, and Dstores
- **Concurrency**: Java’s multithreading for handling concurrent client requests
- **Data Structures**: Custom index for managing file states and allocation

## How It Works
1. **Controller**: Orchestrates client requests and manages file distribution across Dstores. It handles tasks such as file storage, load balancing, and monitoring the health of Dstores.
2. **Dstores**: Store the actual file data and communicate with the Controller to process client requests.
3. **Clients**: Submit file operations (store, load, list, remove) to the Controller, which forwards them to the appropriate Dstores.

### Operations Supported
- **Store File**: Clients send files to be stored across multiple Dstores, ensuring replication.
- **Load File**: Clients retrieve files from one of the Dstores holding the data.
- **Remove File**: Clients request the deletion of a file, which is removed from all Dstores.
- **List Files**: Clients can retrieve a list of all stored files.

## Setup & Usage
### Requirements
- **Java** (openjdk-21 recommended)
- **Linux/Unix environment** (Windows not supported)

### How to Run
1. **Controller**:  
   Run the Controller with the following command:  
   ```java
   java Controller <cport> <replication_factor> <timeout> <rebalance_period>
## Parameters

| Parameter               | Description                             |
|-------------------------|-----------------------------------------|
| `cport`                 | Controller’s listening port             |
| `replication_factor`    | Number of replicas per file             |
| `timeout`               | Timeout in milliseconds                  |
| `rebalance_period`      | Period to trigger the rebalancing process |

2. **Dstore**:
   Run the Controller with the following command:  
   ```java
   java Dstore <port> <controller_port> <timeout> <file_folder>

## Parameters

| Parameter               | Description                             |
|-------------------------|-----------------------------------------|
| `port`                 | Controller’s listening port             |
| `controller_port`      | Number of replicas per file             |
| `timeout`              | Timeout in milliseconds                  |
| `file_folder`          | Period to trigger the rebalancing process |

3. **Client**:
   ```java
   java Client <controller_port> <timeout>

## Example Usage
### Store a File
      To store a file, use the following command:
      ```java
      java Client <controller_port> STORE <filename> <filesize>
### Load a File
      To load a file, use the following command:
      ```java
      java Client <controller_port> LOAD <filename>
### Remove a File
      To remove a file, use the following command:
      ```java
      java Client <controller_port> REMOVE <filename>
### List a File
      To list a file, use the following command:
      ```java
      java Client <controller_port> LIST
## Failure Handling
- If a Dstore fails during a store or remove operation, the Controller automatically adjusts the system to ensure consistency.
- Clients receive appropriate error messages if Dstores are unavailable or if replication requirements are not met.

## Future Improvements
- Support for larger file sizes and distributed operation across multiple machines.
- Enhanced fault tolerance to handle multiple simultaneous Dstore failures.
- Add storage rebalance operation
