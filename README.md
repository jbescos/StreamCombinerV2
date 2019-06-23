# StreamCombinerV2

To compile with test execution:
mvn clean install

To generate the package:
mvn clean compile assembly:single

To execute:
$ java -jar target/streamcombinerv2-1.0-jar-with-dependencies.jar -port 23456 -sockets 4

To connect to it:
You can use any TPC client (for example telnet) to the host:port that the application is running. You can also listen messages coming from the server if you want to be aware of possible errors.

To stop the application gracefully:
Just open a TPC connection to the application reserved port 40356

