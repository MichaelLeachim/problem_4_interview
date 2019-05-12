(ns problem_4_interview.tools
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.tools.logging :refer [info]]
   [jackdaw.serdes.edn :as jse]
   [jackdaw.serdes.resolver :as resolver]
   [jackdaw.client :as jc]
   [jackdaw.streams :as j]
   [jackdaw.client.log :as jcl]
   [jackdaw.admin :as ja]))

(def bootstrap-servers
  (get (System/getenv) "BOOTSTRAP_SERVERS" "localhost:9092"))


(defn topic-config
  "Takes a topic name and (optionally) key and value serdes and a
  partition count, and returns a topic configuration map, which may be
  used to create a topic or produce/consume records."
  ([topic-name]
   (topic-config topic-name (jse/serde)))

  ([topic-name value-serde]
   (topic-config topic-name (jse/serde) value-serde))

  ([topic-name key-serde value-serde]
   (topic-config topic-name 1 key-serde value-serde))

  ([topic-name partition-count key-serde value-serde]
   {:topic-name topic-name
    :partition-count partition-count
    :replication-factor 1
    :topic-config {}
    :key-serde key-serde
    :value-serde value-serde}))

(defn kafka-producer-config
  []
  {"bootstrap.servers" bootstrap-servers})

(defn kafka-admin-client-config
  []
  {"bootstrap.servers" bootstrap-servers})

(defn create-topic!
  "Takes a topic config and creates a Kafka topic."
  [topic-config]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (ja/create-topics! client [topic-config])))

(defn list-topics
  "Returns a list of Kafka topics."
  []
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (ja/list-topics client)))

(defn kafka-consumer-config
  [group-id]
  {"bootstrap.servers" bootstrap-servers
   "group.id" group-id
   "auto.offset.reset" "earliest"
   "enable.auto.commit" "false"})

(defn publish
  "Takes a topic config and record value, and (optionally) a key and
  parition number, and produces to a Kafka topic."
  ([topic-config value]
   (with-open [client (jc/producer (kafka-producer-config) topic-config)]
     @(jc/produce! client topic-config value))
   nil)

  ([topic-config key value]
   (with-open [client (jc/producer (kafka-producer-config) topic-config)]
     @(jc/produce! client topic-config key value))
   nil)

  ([topic-config partition key value]
   (with-open [client (jc/producer (kafka-producer-config) topic-config)]
     @(jc/produce! client topic-config partition key value))
   nil))

(defn get-records
  "Takes a topic config, consumes from a Kafka topic, and returns a
  seq of maps."
  ([topic-config]
   (get-records topic-config 200))
  ([topic-config polling-interval-ms]
   (get-records
    topic-config polling-interval-ms
    (str (java.util.UUID/randomUUID))))
  ([topic-config polling-interval-ms consumer-id ]
   (let [client-config (kafka-consumer-config consumer-id)]
     (with-open [client (jc/subscribed-consumer client-config
                                                [topic-config])]
       (doall (jcl/log client 100 seq))))))

