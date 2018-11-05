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

## Todo List

## Optimizations
- COMMUNICATION: Message ids for parallel GET requests
- DB: separated KEY file for managing keys 
  - Tuple: <KEY,bytePosition>
- DB: Keep keys in memory 
  - a max key length is needed
- DB: Fragmentation Handling in DB file
- MESSAGING: remove need of (un)escaping characters by using unprintable characters in message encoding
- CLIENT: Try reconnect

## Care about possible Bugs
- Escaping in DB-file
- Escaping in messages
- Interrupted messaging / hanging client