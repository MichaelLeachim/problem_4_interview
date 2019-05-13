# problem_4_interview

Problem definition:

https://hackmd.io/s/rJdSetNhM

## How it should work?

* Have an atom that maintains an app state
  `{:id 1 :name "blab" :q "hello" :timestamp 123131231}`
* Store that atom on a specific topic
  (It is a hack, but it is easy to implement the same 
   strategy but on another storage)
* Subscribe to the `:input` channel
* For every message, publish it to a channel, according to the 
  filters. 
* For new channel, create topic, add metadata, 
  store results in app state atom
* When starting up, read up an app state from the atom

### Potential problems:

* Reading from the beginning by creating a new consumer
  will pile up unuzed data in Zookeeper:
  Use `seekToBeginning` instead
  See: https://stackoverflow.com/questions/28561147/how-to-read-data-using-kafka-consumer-api-from-beginning
  
### TODO
  
## Usage

### Run the application locally

```bash

git clone <clone here>
cd <path here>
tmuxinator . 

### Run API

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
