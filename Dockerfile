FROM openjdk:8-jre-alpine
ADD target/scala-2.13/ads-delivery-assembly-0.1.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]