Thanks for your interest in contributing to DemoParty Chat.

Guidelines
- Fork the repository and create a topic branch for your change.
- Keep changes focused and documented.
- Run the test suite and ensure formatting is consistent.

Development
- Build: `./mvnw clean package`
- Run: `./mvnw spring-boot:run`

Encryption and privacy note
- The client-side secret key is intentionally stored only in the browser. Do not add server-side code that transmits or persists user secret keys.
