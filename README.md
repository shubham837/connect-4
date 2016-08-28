# Connect-4

### Brief
Connect-4 Game backend application

Technologies/Library used in Application:<br/>
1) jdk-1.8 </br>
2) Maven as build Tool </br>
3) Spring-boot </br>
4) Spring-data-cassandra
5) Spring-data-redis
5) Apache HttpComponents </br>
6) Cassandra Database as Persistent System
7) Redis for Caching and User Authentication based on auth key and access token

# Setup instructions
1) Install maven build tool and jdk1.8 </br>
2) Install and run Cassandra database(version should be lesser than 3.0) on local system. Add the configurations for cassandra in application.properties file</br>
3) Start Redis on local system (Port used 6379)</br>
4) Clone the repository and pull the latest change. </br>
5) Initialize the initial keyspace and column families in cassandra by running the command `bin/cqlsh -f  ~/personalpro/connect4/database_creation.cql` </br>
5) Build the application using the command `mvn clean install`</br>

