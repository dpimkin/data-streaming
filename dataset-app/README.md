
This project entails the development of a compact and responsive application designed to create a JSON dataset. The application is built to be lightweight and efficient, enabling the generation of datasets in a quick and streamlined manner.

## How to build

To initiate the build process, simply execute the following command in your preferred terminal:

```bash
./gradlew clean build
```

## How to run

Once the build process is complete, the application can be run using the following command:

```bash
java -jar build/libs/dataset-app-0.0.1-SNAPSHOT.jar 
```

## How to invoke 

After the application is up and running, you can invoke its functionality by making a POST request. This can be done using a tool like curl. Execute the following command in your terminal to generate a dataset:


```bash
curl -X POST http://localhost:8081/dataset/28
```

By following these steps, you will be able to utilize the capabilities of this application and create a JSON dataset tailored to your requirements.