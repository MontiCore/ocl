#Build CLI
FROM gradle:6.5.1-jdk8 as build
ADD . /app
WORKDIR /app
RUN gradle build -x test -x javadoc

# Copy it to distroless image
FROM gcr.io/distroless/java:11
COPY --from=build /app/target/libs/OCLCLI.jar /app/OCLCLI.jar
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/OCLCLI.jar"]

