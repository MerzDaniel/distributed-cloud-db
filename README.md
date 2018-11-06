# Practical Course Cloud Databases

A distributed key-value database for the master praktikum "Cloud Databases"

## SETUP
Run tests: 
`ant test`

Build jars for server and client: 
`ant build-jar`

Run the server
`java -jar ms2-server.jar --port 50000 --cache-type LFU --cache-size 10 --log-level WARN`

Run the client
`java -jar ms2-client.jar`

## Folder structure
- `src` contains all source code
- `test` contains all tests
- `logs` contains logs
- `db` contains dbs
- `docs` contains the test descriptions

## Steps

## Todo List
STEP 1
- Lib 
  - Consistent Hashing (Table, key->entry, )
    - calcServerHash, calcKeyHash, getResponsibleServer
  - ClientMessaging
    - new messages for server state
  - ECSAdminMessages
    - Messages: Start,Stop,Shutdown, locking, moveData, update(metadata)
- KV Client 
  - Handle new messages in GET/PUT
- ECS
  - Lib
    - Run Jar with parameters
    - SSH
    - ServerCommunication
    - Configuration File
  - ClientInterface
- KV SERVER
  - KV
    - DB file rename with server hash
    - extend with WriteLock
  - Startup: Wait for configuration
  
STEP 2
- Performance measurements

STEP 3
Performance optimizations, Report

## Optimizations
- COMMUNICATION: Message ids for parallel GET requests
- DB: separated KEY file for managing keys 
  - Tuple: <KEY,bytePosition>
  - Seperate key cache
- DB: Keep keys in memory 
  - a max key length is needed
- DB: Fragmentation Handling in DB file
- MESSAGING: remove need of (un)escaping characters by using unprintable characters in message encoding
- CLIENT: Try reconnect
- Message compression

## Care about possible Bugs
- Escaping in DB-file
- Escaping in messages
- Interrupted messaging / hanging client