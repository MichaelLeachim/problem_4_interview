# problem_4_interview

## Problem definition:

https://hackmd.io/s/rJdSetNhM

## Structure and limitations 

### App structure

We have:

* General topics, not this app related
* Private topics, that are prefixed with the `app.id`
  * Processing topics, that take data from the input topic

### Limitation 1. Metadata

We store filter related information in the name 
of the filtering topic in the following format

`<app.id><f|p>_<timestamp>_<from_topic>_<q>`

In fact we could have used any storage facility, 
but that would imply additional state, additional
syncing of the state, and, in the end, more work, 
which is out of scope of this test task. 

Hence, the limitation on the `q`
It should be alphanumeric and no longer than 


### Limitation 2. 

We use timestamp of the filter creation as a filter ID


Hence, the 




We use Kafka Streams API and DSL to 
implement the solution in the following form: 

* 


In this solution we store topics metadata in the 
topic name in the following form

## How it works?

We have a set of output topics that ma


### TODO
  
## Usage

### Run the application locally

```bash

git clone https://github.com/MichaelLeachim/problem_4_interview
cd problem_4_interview
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
