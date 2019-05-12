# problem_4_interview

FIXME

### TODO

* move all config values to a separate config file
* figure out how to extract timestamp in filter
* figure out how to store metadata in topics 
  (topic_id/topic_name/filter_string/date_of_creation)
* make an Atom to hold the kstream instances: 
  * On load, go through all filters and create kstream instances



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
