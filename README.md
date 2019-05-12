# problem_4_interview

FIXME

## Usage

### Run the application locally

```bash

git clone <clone here>
cd <path here>
tmuxinator . 

# run kafka

cd vendor/kafka;
bin/zookeeper-server-start.sh config/zookeeper.properties; 
bin/kafka-server-start.sh config/server.properties;
```

`lein ring server`

### Run the tests

`lein test`

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

## License

Copyright ©  FIXME
