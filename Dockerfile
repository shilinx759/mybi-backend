# Docker 镜像构建
FROM openjdk:8-jdk-alpine

# Copy local code to the container image.
WORKDIR /app

# 删除之前的镜像文件
RUN rm -rf /app/mybi-backend*

ADD target/mybi-backend-0.0.1-SNAPSHOT.jar mybi-backend.jar

# Build a release artifact.
#RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","mybi-backend.jar","--spring.profiles.active=prod"]