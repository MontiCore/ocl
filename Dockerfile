#Build CLI
FROM registry.git.rwth-aachen.de/monticore/container-registry/gradle:7.6.3-jdk11 as build
ADD . /app
WORKDIR /app
RUN gradle :tool:shadowJar -x test -x javadoc

# Copy it to distroless image
FROM gcr.io/distroless/java:11
COPY --from=build /app/tool/build/libs/MCOCL.jar /app/MCOCL.jar
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/MCOCL.jar"]

