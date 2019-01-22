# Practical Course Cloud Databases

A distributed key-value database for the master seminar "Cloud Databases"

## SETUP
Run tests: 
`ant test`

Build jars for server, client and ecs: 
`ant build-jar`

Run the server
`java -jar server.jar --port 50000 --cache-type LFU --cache-size 10 --log-level WARN`

Run the client
`java -jar client.jar`

Run the ECS (make sure `ecs.config` file is available in the root folder)
`java -jar ecs.jar`

## Folder structure
- `src` contains all source code
- `test` contains all tests
- `logs` contains logs
- `db` contains dbs
- `docs` contains the performance evaluation
- `tools` contains performance tools
