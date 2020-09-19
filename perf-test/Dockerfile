FROM openjdk:8-jre-alpine
RUN apk --no-cache add curl
ADD target/scala-2.13/ads-stats-perf-test-assembly-0.1.0.jar app.jar
ADD wait-for-server.sh wait-for-server.sh