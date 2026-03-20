This repository contains links to Flowset Control resources and supporting configuration files for running Docker Compose as a local development option.

## Using Docker Compose

The following Docker Compose files are provided:
1. [docker-compose-core.yaml](docker-compose-core.yaml) which contains these components:
    - **Flowset Control**
    - PostgresQL which is used by **Flowset Control**

   **Note:** This Docker Compose is suitable for cases when you need to connect to already running external Camunda 7 engines.
2. [docker-compose-full.yaml](docker-compose-full.yaml) which contains these components:
    - **Flowset Control**
    - PostgresQL which is used by **Flowset Control**
    - Camunda 7 as the external BPM engine - an engine that runs on the `8082` port by default.
    - PostgresQL which is used by Camunda 7

## Running using Docker Compose

1. Clone this repository
2. Open a terminal 
3. Build a JAR from the sources using the following command (run from the root directory):
   ```shell
   .\gradlew clean bootJar -PbuildType=docker -Pvaadin.productionMode=true
   ```
   > For Windows, you need to use quotes
   >
   > ```shell
   > .\gradlew clean bootJar -PbuildType=docker "-Pvaadin.productionMode=true"
   >```

   > You might encounter the `Execution failed for task ':vaadinBuildFrontend'.` error,
   >
   > This `com.vaadin.flow.server.ExecutionFailedException: PWA icons generation failed` error can occur in projects with Jmix version 2.5 while executing Gradle task `vaadinBuildFrontend` with enabled flag `vaadin.productionMode`. It is related to the [Vaadin issue](https://github.com/vaadin/flow/issues/20842).
   >
   > To fix it run the Gradle command with additional flags `--no-build-cache --no-daemon` and add the following property to gradle.properties in the project:
   >```shell
   > org.gradle.jvmargs=-Xmx1024M
   > ```

4. Go to the `docker-compose` directory:
   ```shell 
   cd docker-compose
   ```
5. Execute the following commands:
     - **Flowset Control only:** If you want to run Flowset Control without the Camunda 7 engine, use:
        ```shell
        docker compose -f docker-compose-core.yaml up -d
        ```
        To check container statuses:
        ```shell
        docker container ls -f "name=flowset-control-app" -f "name=flowset-control-database"
        ```
    - **Flowset Control + external Camunda 7:** If you want to run not only Flowset Control, but also the Camunda 7 engine, use
      ```shell
      docker compose -f docker-compose-full.yaml up -d
      ```
      To check container statuses:
      ```shell
      docker container ls -f "name=flowset-control-app" -f "name=flowset-control-database" -f "name=camunda7-bpm-platform" -f "name=camunda7-bpm-platform-database"
      ```
6. Open Flowset Control in your browser using the link [http://localhost:8081](http://localhost:8081) and login as `admin/admin`.
7. Configure your first connection to the Camunda 7 engine. 
   
   If you are running Camunda 7 using `docker-compose-full.yaml`, enter the following data:
   - **Name**: any short name for the configuring engine, e.g. `Dev stand`
   - **Base URL**: `http://camunda7-bpm-platform:8082/engine-rest`
   - **Authentication**: Enabled
   - **Authentication type**: Basic
   - **Username:** `admin`
   - **Password:** `admin`
8. Click the **Test connection** to check that values are correct and the BPM engine is available.
9. Save the Camunda 7 connection configuration.

Now you can deploy processes to the configured BPM engine and manage process instances running on this engine.

## Additional configuration
You can explore the environment variables used by services in the `.env` file and change them to run locally if necessary.
