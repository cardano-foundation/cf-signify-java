# cf-signify-java

## Overview
This project is a Java implementation of **Signify**, a library designed for "signing at the edge" within the **KERI (Key Event Receipt Infrastructure)** ecosystem. It is based on the TypeScript implementation [signify-ts](https://github.com/WebOfTrust/signify-ts) and provides similar functionality for Java developers.

Signify focuses on two key functions of a KERI agent:
1. **Key generation**: Generating cryptographic key pairs for signing and encryption.
2. **Event signing**: Signing KERI events using the generated keys.

The library uses **libsodium** for cryptographic operations, including generating Ed25519 key pairs for signing and X25519 key pairs for encrypting private keys. The encrypted keys are stored remotely, ensuring that the private keys are never exposed to the cloud agent.

This project is built using **Java 21** and **Gradle** as the build tool.

---

## Prerequisites
- **Java 21**: Ensure you have Java 21 installed. You can verify your Java version by running:
  ```bash
  java -version
  ```
- **Gradle**: This project uses Gradle as the build tool. You can install Gradle by following the instructions on the [Gradle website](https://gradle.org/install/).

## Building the Project
To build the project, navigate to the project directory and run:

```bash
./gradlew build
```

This will compile the source code, run the tests, and package the application.

## Running Unit tests
Unit tests are an essential part of the development process. To run the unit tests, execut

```bash
./gradlew clean test
```
This command will run all the tests in the project and provide a summary of the results.

## Build Docker when run E2E tests
The integration tests depends on a local instance of KERIA, vLEI-Server and Witness Demo. These are specified in the [Docker Compose](./docker-compose.yaml) file. To start the dependencies, use docker compose:

```bash
docker compose up --wait
```

If successful, it should print someting like this:

```bash
$ docker compose up --wait
[+] Running 4/4
 ✔ Network signify-ts_default           Created                                           0.0s
 ✔ Container signify-ts-vlei-server-1   Healthy                                           5.7s
 ✔ Container signify-ts-keria-1         Healthy                                           6.2s
 ✔ Container signify-ts-witness-demo-1  Healthy                                           6.2s
```

It is possible to change the keria image by using environment variables. For example, to use weboftrust/keria:0.1.3, do:

```bash
export KERIA_IMAGE_TAG=0.1.3
docker compose pull
docker compose up --wait
```

To use another repository, you can do:

```bash
export KERIA_IMAGE=gleif/keria
docker compose pull
docker compose up --wait
```
## Running E2E tests
Use the gradlew script "test E2E" to run all E2E tests in sequence:
```bash
./gradlew clean testE2E
```


## Generate Allure Report
Use the gradlew script "Allure Serve" to view the test result report:
```bash
./gradlew allureServe
```