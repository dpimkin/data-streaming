
## How to build

```bash
./gradlew clean build --info 
```


## How to run

```bash
mongod --bind_ip_all --noauth --dbpath ./db
```

```bash
java -jar build/libs/dbproc-0.0.1-SNAPSHOT.jar
```



```bash
curl -X POST http://localhost:8080/test -H "Content-type: application/json" -d "[{ \"payload\": \"test1\" }]"
```