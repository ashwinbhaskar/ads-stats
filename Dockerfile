FROM openjdk:8-jre-alpine
ADD target/scala-2.13/ads-delivery-assembly-0.1.0.jar app.jar
ADD src/main/resources/application-docker.conf application-docker.conf
ENTRYPOINT ["java","-Dconfig.file=/application-docker.conf","-jar","/app.jar"]