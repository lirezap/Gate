FROM ghcr.io/graalvm/jdk-community:25

RUN mkdir /opt/app
WORKDIR /opt/app

COPY target/gate-*.jar gate.jar

CMD ["java", "-jar", "-XX:+UseCompactObjectHeaders", "gl.jar"]
