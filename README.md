# Practical Course Cloud Databases

A distributed key-value database for the master praktikum "Cloud Databases"

## Todo List
- CLIENT: PUT_UPDATE!
- DB: Serializing of data before save (because of =,\n signs)
  - or just prohibit non-printable characters
- JavaDoc comments everywhere
- CLIENT: Check max key/value length! ( 20bytes / 120kb)
- SERVER: Reject wrong key/value!  
- Fix provided tests

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