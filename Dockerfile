FROM ghcr.io/graalvm/jdk-community:24

RUN mkdir /opt/app
WORKDIR /opt/app

COPY target/gate-*.jar gate.jar

CMD ["java", "-jar", "gate.jar"]
