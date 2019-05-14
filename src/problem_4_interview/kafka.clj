;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-14 14:27 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

;; Inspired by https://github.com/FundingCircle/jackdaw/tree/master/examples/dev

(ns problem_4_interview.kafka
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [jackdaw.serdes.edn :as jse]
   [jackdaw.serdes.resolver :as resolver]
   [jackdaw.client :as jc]
   [jackdaw.streams :as j]
   [jackdaw.admin :as ja]))

;; ===================== Config =========================================
(defn get-env
  [param default]
  (get (System/getenv) param default))

(def bootstrap-servers
  (get-env "BOOTSTRAP_SERVERS" "localhost:9092"))

(defn kafka-producer-config
  []
  {"bootstrap.servers" bootstrap-servers})

(defn kafka-admin-client-config
  []
  {"bootstrap.servers" bootstrap-servers})

(defn kafka-consumer-config
  [group-id]
  {"bootstrap.servers" bootstrap-servers
   "group.id" group-id
   "auto.offset.reset" "earliest"
   "enable.auto.commit" "true"})

(def application-config
  {"application.id"            (get-env "APPLICATION_ID" "problem-4-interview")
   "bootstrap.servers"         bootstrap-servers
   "default.key.serde"         "jackdaw.serdes.EdnSerde"
   "default.value.serde"       "jackdaw.serdes.EdnSerde"
   "cache.max.bytes.buffering" "0"})

(defn make-topic-config
  "Takes a topic name and (optionally) key and value serdes and a
  partition count, and returns a topic configuration map, which may be
  used to create a topic or produce/consume records."
  ([topic-name]
   (make-topic-config topic-name (jse/serde)))

  ([topic-name value-serde]
   (make-topic-config topic-name (jse/serde) value-serde))

  ([topic-name key-serde value-serde]
   (make-topic-config topic-name 1 key-serde value-serde))

  ([topic-name partition-count key-serde value-serde]
   {:topic-name topic-name
    :partition-count partition-count
    :replication-factor 1
    :topic-config {}
    :key-serde key-serde
    :value-serde value-serde}))



;; ======================= Mutable actions ===============================

(defn create-topic!
  "Takes a topic config and creates a Kafka topic."
  [topic-config]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (ja/create-topics! client [topic-config])))

(defn create-topic-if-not-exists!
  "Takes a topic config and creates a Kafka topic."
  [topic-config]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (if-not (ja/topic-exists? client topic-config)
      (ja/create-topics! client [topic-config]))))

(defn delete-topic!
  [topic]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (ja/delete-topics! client [topic])))

(defn publish!
  "Takes a topic config and record value, and (optionally) a key and
  parition number, and produces to a Kafka topic."
  ([topic-config value]
   (publish! topic-config nil value))
  ([topic-config key value]
   (with-open [client (jc/producer (kafka-producer-config) topic-config)]
     @(jc/produce! client topic-config key value))
   nil))

;; ======================= Streams =====================================

(defn- build-filter-map-fn
  [builder from-topic to-topic predicate filter-map-fn]
  (let [input-stream  (j/kstream builder from-topic)
        predicate-fn
        (predicate input-stream
                   (fn [[k v]]
                     (filter-map-fn k v)))
        _ (j/to predicate-fn to-topic)]
    builder))
(defn- create-stream!
  [from-topic to-topic predicate filter-map-fn]
  (let [builder (j/streams-builder)
        _ (create-topic-if-not-exists! to-topic)
        _ (build-filter-map-fn builder from-topic to-topic predicate filter-map-fn)
        app (j/kafka-streams builder application-config)
        _ (j/start app)]
    app))

(defn create-filter-stream!
  [from-topic-str to-topic-str filter-fn]
  (create-stream! (make-topic-config from-topic-str)
                  (make-topic-config to-topic-str)
                  j/filter filter-fn))

(defn create-map-stream!
  [from-topic-str to-topic-str map-fn]
  (create-stream! (make-topic-config from-topic-str)
                  (make-topic-config to-topic-str)
                  j/map map-fn))

;; ======================= Immutable helpers ==============================

(defn topic-exists?
  "Takes a topic name and returns true if the topic exists."
  [topic-config]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (ja/topic-exists? client topic-config)))

(defn list-topics
  "Returns a list of Kafka topics."
  []
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (ja/list-topics client)))

(defn list-records
  ([topic-config]
   (list-records topic-config (str (java.util.UUID/randomUUID))))
  ([topic-config consumer-id]
   (let [client-config (kafka-consumer-config consumer-id)]
     (with-open [client (jc/subscribed-consumer client-config
                                                [topic-config])]
       (jc/poll client 200)))))

(defn list-topic-vals
  [topic]
  (map :value (list-records topic)))

(comment
  
  (map :value (list-records (make-topic-config "input")  "reader"))
  
  (map :value (list-records (make-topic-config "java")  "reader"))
  
  (delete-topic! (make-topic-config "java"))
  (create-topic! (make-topic-config "input"))
  
  (publish! (make-topic-config "input")  "java")
  
  (map :value (get-records (make-topic-config "bbb")))
  (defn mapper  [key val]
    (clojure.string/upper-case val))
  
  (def uppercase
    (create-map-stream!
     "input"
     "uppercase"
     mapper))
  
  (def time-filter 
    (create-filter-stream!
     (make-topic-config "input")
     (make-topic-config "java")
     (fn [key val]
       (clojure.string/includes? (clojure.string/lower-case val) "java"))))  
  
  (j/close java-filter)
  
  (list-topic-vals (make-topic-config "java"))
  (list-topic-vals (make-topic-config "input"))
  (list-topic-vals (make-topic-config "uppercase"))
  (publish! (make-topic-config "input") "java333")
  (delete-topic! (make-topic-config "java"))
  (j/close java-filter)
  
  (map :value (list-records (make-topic-config "input") 200 ))
  
  
  )


