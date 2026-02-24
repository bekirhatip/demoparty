# DemoParty Chat

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)


**DemoParty Chat** is a lightweight, open-source WebSocket chat server implemented with Spring Boot. It is designed for privacy-conscious, real-time communication, optional end-to-end client-side encryption and featuring room-based messaging without server-side persistence. It is small, open-source WebSocket chat server implemented with Spring Boot. It provides room-based real-time chat, optional client-side message encryption, basic spam protection, and IP-based session restrictions. Messages are broadcast to the room and are not persisted on the server.

---

## Features
* **Real-Time Messaging:** Powered by Spring Boot WebSockets for instantaneous communication.
* **Zero Persistence:** Messages are broadcast to the room and are **never** saved on the server or in a database.
* **Client-Side Encryption (E2EE):** * Optional encryption using AES.
    * Secret keys are stored strictly in the browser and are **never** transmitted to the server.
* **Spam Protection:** Built-in rate-limiting and heuristics to mitigate abusive traffic.
* **IP-Based Restrictions:** Prevents multiple simultaneous sessions from the same IP address for enhanced security.
* **Automatic Decryption:** The client identifies encrypted payloads and attempts decryption locally if a key is provided.

## Technology
- Java + Spring Boot
- WebSocket for real-time messaging
- Minimal static client shipped in `src/main/resources/static`


## 🌐 Live Demo

Experience the application in action:  
🔗 **[Try the Live Demo](https://demoparty.bekir.work/)**


---
## Quick start

### Requirements:
- Java 17 or newer
- Maven (optional — the project includes the Maven Wrapper)

### Build and run with Java

**Using Maven Wrapper:**
```
./mvnw spring-boot:run
```
**or As a JAR Package:**
```
./mvnw clean package
java -jar target/*-SNAPSHOT.jar
```

Open http://localhost:8080 to see the client.

### with a Docker

***For Docker Build***
```
docker build -t demoparty-chat .
```

***For Docker Run***
```
docker compose up -d
```
***or***
```
docker run -p 8080:8080 demoparty-chat
```

---

### Configuration
- See `src/main/resources/application.properties` for default settings.
- Common settings: `server.port`, rate limit and IP restriction options (see `IpHandshakeInterceptor` and `RateLimitState` in source).

## Privacy & Security
- The server does not persist messages; messages are relayed to connected clients only.
- The secret key used for client-side encryption is stored only in the browser (client-side storage) and never transmitted to the server.
- The project includes basic spam protection and IP session restrictions but is not intended to be a hardened security appliance; evaluate and extend for production use.

## Encryption

DemoParty Chat supports optional client-side encryption. The following describes how encryption is used in the application and how the client behaves when sending and receiving messages.

- Secret key storage:
When a user enters a secret key in the client UI, it is stored only in the browser (in memory or local storage under the user's control). The secret key is never sent to the server.

- Encrypted message identification:
Encrypted messages are identified by a special prefix. Any message that starts with the literal string ENC:: is treated as encrypted. Messages without this prefix are treated as plaintext.

- Sending Messages:
    - If no secret key is set in the browser, the client sends plaintext messages.

    - If a secret key is set, the client encrypts the message using AES and prefixes the resulting ciphertext with ENC:: before sending it to the server.

    - The server receives and relays the message payload exactly as sent. It does not modify, inspect, encrypt, or decrypt the message.

- Receiving Messages:
    - If a received message does not start with ENC::, the client treats it as plaintext and displays it immediately.

    - If a received message does start with ENC::, the client:

        - Removes the prefix.

        - Attempts to decrypt the remaining ciphertext using the secret key currently stored in the browser.

        - If decryption succeeds, the plaintext message is displayed.

        - If decryption fails (for example due to an incorrect secret key), the client shows a clear UI indicator that the message could not be decrypted.

    - The application never sends the secret key to the server and does not attempt key guessing.

- ***Server Role:***
    - The server acts purely as a relay:
    - It broadcasts messages to connected clients.
    - It does not store secret keys.
    - It does not perform encryption or decryption.
    - It does not inspect encrypted message contents.


## Contributing
- See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines and development notes.

## License
- This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
