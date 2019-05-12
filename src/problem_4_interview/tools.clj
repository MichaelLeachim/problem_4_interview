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

;; ===================== Config =========================================

(def bootstrap-servers
  (get (System/getenv) "BOOTSTRAP_SERVERS" "localhost:9092"))
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
   "enable.auto.commit" "false"})

(def application-config
  {"application.id"            "problem-4-interview"
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

;; ====================== Misc ============================================

(defn- build-filter-fn
  [builder from-topic to-topic filter-fn]
  (let [input-stream   (j/kstream builder from-topic)
        filter-fn
        (j/filter input-stream
                  (fn [[k v]]
                    (filter-fn k v)))
        _ (j/to filter-fn to-topic)]
    builder))

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

(defn create-filter-stream!
  [from-topic to-topic filter-fn]
  (let [builder (j/streams-builder)
        _ (create-topic-if-not-exists! to-topic)
        _ (build-filter-fn builder from-topic to-topic filter-fn)
        app (j/kafka-streams builder application-config)
        _ (j/start app)]
    app))


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

(defn list-records-from-the-beginning
  "Takes a topic config, consumes from a Kafka topic, and returns a
  seq of maps.
  mik: 

    making consumer-id random with auto.offset.reset=earliest
    will make every call to get-records read the topic from the beginning. 
    On the other hand, there might be problem with stale consumers. 

    See more:
      https://stackoverflow.com/questions/28561147/how-to-read-data-using-kafka-consumer-api-from-beginning"
  
  ([topic-config]
   (list-records-from-the-beginning topic-config 200))
  ([topic-config polling-interval-ms]
   (list-records-from-the-beginning
    topic-config polling-interval-ms
    (str (java.util.UUID/randomUUID))))
  ([topic-config polling-interval-ms consumer-id ]
   (let [client-config (kafka-consumer-config consumer-id)]
     (with-open [client (jc/subscribed-consumer client-config
                                                [topic-config])]
       (let [result  (doall (jcl/log client 100 seq))
             _ (.close client)]
         result)))))


(defn list-topic-vals
  [topic]
  (map :value (list-records-from-the-beginning topic)))

(comment
  
  (list-topics )
  
  (delete-topic! (make-topic-config "java"))
  (create-topic! (make-topic-config "input"))
  
  (publish! (make-topic-config "input")  "java")
  
  (map :value (get-records (make-topic-config "bbb")))
  (def java-filter
    (create-filter-stream!
     (make-topic-config "input")
     (make-topic-config "java")
     (fn [key val]
       (clojure.string/includes? (clojure.string/lower-case val) "java"))))
  (j/close java-filter)
  
  (list-topic-vals (make-topic-config "java"))
  (list-topic-vals (make-topic-config "input"))
  (publish! (make-topic-config "input") "java333")
  (delete-topic! (make-topic-config "java"))
  (j/close java-filter)
  
  (map :value (list-records-from-the-beginning (make-topic-config "input")))
  
  
  )

