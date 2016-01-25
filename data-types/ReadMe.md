# This project in a work in progress

The intention: to declare loose message types for a multi-developer project. 
Each developer can incrementally forward the type system (backwards-compatible) and hot deploy their work to a common cluster
without breaking existing dependencies. True microservice development.

The design: each type is declared with a json-schema file. A generator scans the files and generates wrapper types for 
JsonObject storing the type. The wrappers can be used to construct a well formed instance of the type; to validate the JsonObject
on construction. Access is always to weakly typed JsonObject and this can be retrieved at any time. 

