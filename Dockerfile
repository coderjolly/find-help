FROM ibm-semeru-runtimes:open-11-jre-focal
COPY target/findhelp-0.0.1-SNAPSHOT.jar findhelp-0.0.1-SNAPSHOT.jar
ENV _JAVA_OPTIONS="-XX:MaxRAM=70m"
CMD java $_JAVA_OPTIONS -jar findhelp-0.0.1-SNAPSHOT.jar