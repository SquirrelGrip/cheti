FROM openjdk:11
COPY target/cheti-1.1.12-SNAPSHOT.jar /tmp/lib/
COPY target/dependencies/* /tmp/lib/
COPY cheti.json /tmp
WORKDIR /tmp
CMD ls -l lib;java --class-path lib/cheti-1.1.12-SNAPSHOT.jar:lib/*.jar com.github.squirrelgrip.cheti.Cheti /tmp/cheti.json