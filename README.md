# Practical Course Cloud Databases

A distributed key-value database for the master praktikum "Cloud Databases"

## Todo List
- Client: Follow client-protocol 
- Server: Follow server-protocol 
- DB: Serializing of data before save (because of =,\n signs)
  - or just prohibit non-printable characters
- DB: Caching
  - FIFO
  - LRU (least reacently used)
  - LFU (least frequently used)
- PDF: Compiled short test description of TestCases implemented
- JavaDoc comments everywhere

## Optimizations
- COMMUNICATION: Message ids for parallel GET requests
- DB: seperated KEY file for managing keys 
  - Tuple: <KEY,bytePosition>
- DB: Keep keys in memory 
  - a max key length is needed
- DB: Fragmentation Handling in DB file
- MESSAGING: remove need of (un)escaping characters by using unprintable characters in message encoding

## Care about possible Bugs
- Escaping in DB-file
- Escaping in messages
- Interrupted messaging / hanging client