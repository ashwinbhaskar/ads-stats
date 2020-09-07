ARG no_op_tracer=false
FROM openjdk:8-jre-alpine
ADD target/scala-2.13/ads-stats-assembly-0.1.0.jar app.jar
ADD src/main/resources/application-docker.conf application-docker.conf
ENTRYPOINT ["java","-Dconfig.file=/application-docker.conf","-Duse_no_op_tracer=$no_op_tracer","-jar","/app.jar"]