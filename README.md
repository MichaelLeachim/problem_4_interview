# problem_4_interview

## Problem definition:

https://hackmd.io/s/rJdSetNhM

## Usage

### Run the application locally

```bash

git clone https://github.com/MichaelLeachim/problem_4_interview
cd problem_4_interview

tmuxinator . 

````

Tmuxinator will run Zookeeper and a Kafka server instance 
for the local development purposes. 


### Run API

`lein ring server`

### Run the tests

`lein test-refresh`

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar

```

### Packaging as war

`lein ring uberwar`

