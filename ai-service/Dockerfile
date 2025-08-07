FROM eclipse-temurin:21 AS builder
WORKDIR /application
COPY target/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:21
WORKDIR /application

# Install gcloud
RUN apt-get update \
    && apt-get install -y curl gnupg python3 \
    && curl -sSL https://sdk.cloud.google.com | bash \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /application

# Make gcloud available
ENV PATH=/root/google-cloud-sdk/bin:$PATH

COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}
ENV SPRING_PROFILES_ACTIVE=docker

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "org.springframework.boot.loader.launch.JarLauncher"]
