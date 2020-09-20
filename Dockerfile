FROM openjdk:8-jre-alpine
ARG no_op_tracer=false
ENV use_no_op_tracer=$no_op_tracer
ADD target/scala-2.13/ads-stats-assembly-0.1.0.jar app.jar
ADD src/main/resources/application-docker.conf application-docker.conf
ENTRYPOINT java -Dconfig.file=/application-docker.conf -Duse_no_op_tracer=$use_no_op_tracer -Dcom.sun.management.jmxremote.rmi.port=9090 -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=9090 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.local.only=false -jar /app.jar