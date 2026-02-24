DemoParty Chat
===============

DemoParty Chat is a small, open-source WebSocket chat server implemented with Spring Boot. It provides room-based real-time chat, optional client-side message encryption, basic spam protection, and IP-based session restrictions. Messages are broadcast to the room and are not persisted on the server.

Features
- WebSocket chat using Spring Boot
- Broadcast messages to the room without saving them server-side
- Optional client-side encryption: when a secret key is entered in the browser UI messages are encrypted in the browser and sent encrypted; the key is stored only in the browser and is never sent to the server
- Messages arriving encrypted are decrypted client-side using the stored key before being displayed
- Spam protection: rate-limiting and heuristics to reduce abusive traffic
- IP restriction: prevents multiple simultaneous sessions from the same IP

Technology
- Java + Spring Boot
- WebSocket for real-time messaging
- Minimal static client shipped in `src/main/resources/static`

Quick start

Requirements
- Java 17 or newer
- Maven (optional — the project includes the Maven Wrapper)

Build and run

```
DemoParty Chat
===============

DemoParty Chat is a small, open-source WebSocket chat server implemented with Spring Boot. It provides room-based real-time chat, optional client-side message encryption, basic spam protection, and IP-based session restrictions. Messages are broadcast to the room and are not persisted on the server.

Features
- WebSocket chat using Spring Boot
- Broadcast messages to the room without saving them server-side
- Optional client-side encryption: when a secret key is entered in the browser UI messages are encrypted in the browser and sent encrypted; the key is stored only in the browser and is never sent to the server
- Messages arriving encrypted are decrypted client-side using the stored key before being displayed
- Spam protection: rate-limiting and heuristics to reduce abusive traffic
- IP restriction: prevents multiple simultaneous sessions from the same IP

Technology
- Java + Spring Boot
- WebSocket for real-time messaging
- Minimal static client shipped in `src/main/resources/static`

Quick start

Requirements
- Java 17 or newer
- Maven (optional — the project includes the Maven Wrapper)

Build and run

```
./mvnw clean package
./mvnw spring-boot:run
```

Open http://localhost:8080 to see the client.

Docker

```
docker build -t demoparty-chat .
docker run -p 8080:8080 demoparty-chat
```

Configuration
- See `src/main/resources/application.properties` for default settings.
- Common settings: `server.port`, rate limit and IP restriction options (see `IpHandshakeInterceptor` and `RateLimitState` in source).

Privacy & Security
- The server does not persist messages; messages are relayed to connected clients only.
- The secret key used for client-side encryption is stored only in the browser (client-side storage) and never transmitted to the server.
- The project includes basic spam protection and IP session restrictions but is not intended to be a hardened security appliance; evaluate and extend for production use.

Encryption
---------

DemoParty Chat supports optional client-side encryption. The following describes how encryption is used in the application and what the user interface does on send and receive.

- Secret key storage: when a user enters a secret key in the client UI it is stored only in the browser (in memory or local client storage under the user's control). The secret key is never sent to the server.

- Key derivation and salt: encrypted messages include a randomly-generated salt and an IV (initialization vector) as part of the message envelope. The salt is used to derive an AES key from a user-provided passphrase (for example via PBKDF2). The salt and IV travel with the encrypted message so other clients in the room that possess the same secret key can derive the same AES key and decrypt.

- Sending messages:
	- If no secret key is set in the browser the client sends plaintext messages. Encrypted-message metadata (salt/iv) may also be relayed when present, but the message body is plain text.
	- If a secret key is set the client derives a symmetric AES key from the passphrase and salt, encrypts the message (AES in an AEAD mode such as AES-GCM is recommended), and formats the payload so that the server and other clients can identify it as encrypted.
	- Encrypted payloads are prefixed with the literal string `ENC::` (for example: `ENC::...base64-blob...`). The encoded payload should include the salt, iv, ciphertext and authentication tag so recipients can attempt decryption.

- Receiving messages:
	- When a message arrives that does not start with `ENC::` the client treats it as plaintext and displays it immediately in the chat interface.
	- When a message arrives that begins with `ENC::` the client recognizes it as encrypted and attempts to decrypt it using the secret key currently stored in the browser:
		- The client extracts the salt and IV from the message envelope, derives the AES key from the stored secret, and runs the authenticated decryption.
		- If decryption succeeds, the decrypted plaintext is shown in the chat feed as the message content.
		- If decryption fails (for example because the sender used a different secret key), the client shows a clear indicator in the UI that the message could not be decrypted and that it was sent with a different secret key. The application does not attempt to guess keys or send the secret to the server.

- Relay behavior and server role:
	- The server acts only as a relay. It receives message payloads and broadcasts them to the room. The server does not perform encryption or decryption and does not store secret keys.
	- The server does relay the salt and IV that are included in encrypted messages so that other clients can derive the correct key and decrypt locally.

- UX notes and recommendations:
	- Make sure users understand that the secret key is the sole means to decrypt messages: losing the key means encrypted messages cannot be recovered.
	- Because the key is stored in the browser, protect the client against XSS and other client-side attacks; a compromised browser can leak keys.
	- Use an authenticated encryption mode (AES-GCM or ChaCha20-Poly1305) so tampering is detected. Always verify the authentication tag on decrypt.

Contributing
- See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines and development notes.

License
- This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
