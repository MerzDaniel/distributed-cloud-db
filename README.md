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

## Graph Database Extension

The datastore is extended as a graph database which could store structured data and maintain associations among data. This gives the ability to perform powerful queries over data which was not possible with the previous simple keyValue store. The existing KVClient is extended with two command `MUTATE` to save/modify data and `QUERY` to query data

- run the server  
`java -jar server.jar --port 50000 --cache-type LFU --cache-size 10 --log-level WARN`

- run the client
`java -jar client.jar`

- We can create new data using `MUTATE` command
     
    The syntax
    ```ruby
        MUTATE <documentId> <key1>:<property1>,<key2>,<property2>,.....
    ```
    
    Example : Consider the following data that, we need to store
    ```
        {id:john@wiz.com,firstName:John,lastName:Doe,Occupation:Engineer}
    ```
    
    The query for storing data
    ```ruby
        MUTATE john@wiz.com firstName:John,lastName:Doe,Occupation:Engineer
    ```
    You can also specify JSON objects as properties 
    ```ruby
        MUTATE philip@gmail.com age:30,name:{firstName:Philip,lastName:Crow}
    ```
- `MUTATE` command also can be used to add new properties or modify existing properties

    The Syntax
    ```ruby
        MUTATE <documentIdToModify> <existingkey1>:<modifiedProperty1>,<newKey02>:<newProperty2>,....
    ```
    
    Example :  
    ```  ruby
        MUTATE john@wiz.com lastName:Davidson,age:45
    ```
    The above query would modify the existing property `lastName` and also add a new property `age`
    
- We can query data using `QUERY` command

    The Syntax
    ```ruby
        QUERY <documentId> <key1>,<key2>,...
    ```
    Example : Consider following data already in the graph database with a documentId `john@wiz.com`
    ```
        john@wiz.com:{firstName:John,lastName:Doe,Occupation:Engineer}
    ```
    
    The query to retrieve `firstName` and `lastName`
    ```ruby
        QUERY john@wiz.com firstName,lastName
    ```    
    The result
    ```
        {
          firstName: John,
          lastName: Doe
        }
    ```
    
- Creating associations between data
    
    The Syntax
    ```ruby
        MUTATE <documentId> <key>:<documetIdOfTheAssociatedObject>
    ```
    
    Example : Consider we have below data already in the database
    ```
        john@wiz.com:{firstName:John,lastName:Doe,Occupation:Engineer}
        anna@gmail.com:{firstName:Anna,age:34}
    ```
    
    Associate `anna@gmail.com` as John's best friend
    ```ruby
        MUTATE john@wiz.com bestFriend:anna@gmail.com
    ```
    
    Now we can query this data
    ```ruby
        QUERY john@wiz.com bestFriend
    ```
    The result would be
    ```
        {
          bestFriend: anna@gmail.com
        }
    ```
    We can query the properties of associated objects using `FOLLOW`
    
    Example :
    ```ruby
        QUERY john@wiz.com bestFriend|FOLLOW{firstName,age}
    ```
    Which would give
    ```
    {
      bestFriend: {
        firstName: Anna,
        age: 32
      }
    }
    ```
    
    
    