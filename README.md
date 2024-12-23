# cf-signify-java

## Overview
This project is built using Java 21 and Gradle. 

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
./gradlew test
```
This command will run all the tests in the project and provide a summary of the results.

## Running the e2e tests
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

Use the gradlew script "test E2E" to run all E2E tests in sequence:

```bash
./gradlew testE2E --warning-mode all
```