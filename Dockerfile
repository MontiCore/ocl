#Build CLI
FROM gradle:6.5-jdk11 as build
ADD . /app
WORKDIR /app
RUN gradle build -x test -x javadoc

# Copy it to distroless image
FROM gcr.io/distroless/java:11
COPY --from=build /app/target/libs/MCOCL.jar /app/MCOCL.jar
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/MCOCL.jar"]

