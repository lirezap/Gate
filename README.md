### Gate to Access GL/OMS

A lightweight HTTP gateway for dispatching messages to GL/OMS projects. The main objective is to distinguish the heavy
processing logic of mentioned projects from tasks like binary protocol handling, digital signature verification and REST
API providing. All these features is implemented into this project, Thereby it can easily have multiple instances
simultaneously.

---

### Development Setup (macOS)

- JDK 25
    - ```curl -s "https://get.sdkman.io" | bash```
    - ```sdk install java 25-graalce```

```bash
./mvnw clean package
```

You are now ready to import the project into your IDE to develop/test further.
