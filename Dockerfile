FROM openjdk:8-jre-slim
#Install curl for health check
RUN apt-get update && apt-get install -y --no-install-recommends curl
ADD build/libs/transitdata-db-monitoring-all.jar /usr/app/db-monitoring-all.jar
ENTRYPOINT ["java", "-Xms256m", "-Xmx4096m", "-jar", "/usr/app/db-monitoring-all.jar"]
