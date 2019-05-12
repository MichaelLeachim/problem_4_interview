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
  [topic-name]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (ja/delete-topics! client [{:topic-name topic-name}])))

(defn publish!
  "Takes a topic config and record value, and (optionally) a key and
  parition number, and produces to a Kafka topic."
  ([topic-config value]
   (publish! topic-config nil value))
  ([topic-config key value]
   (with-open [client (jc/producer (kafka-producer-config) topic-config)]
     @(jc/produce! client topic-config key value))
   nil))

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

(defn topology-builder
  "Takes a topic metadata function and returns a function that builds
  the topology."
  [topic-metadata]
  (fn [builder]
    (let [text-input (-> (j/kstream builder (:input topic-metadata))
                         (j/peek (fn [[k v]] (info (str {:key k :value v})))))

          counts (-> text-input
                     (j/flat-map-values split-lines)
                     (j/group-by (fn [[_ v]] v))
                     (j/count))]

      (-> counts
          (j/to-kstream)
          (j/to (:output topic-metadata)))
      builder)))

(defn filter-to
  [from-topic to-topic filter-fn]
  (let [builder        (j/streams-builder)
        input-stream   (j/kstream builder from-topic)
        filter-fn
        (j/filter input-stream
                  (fn [[key val]]
                    (filter-fn key val)))
        _ (j/to filter-fn to-topic)]
    builder))


(defn start-app
  "Starts the stream processing application."
  [topic-metadata app-config]
  (let [builder (j/streams-builder)
        topology ((topology-builder topic-metadata) builder)
        app (j/kafka-streams topology app-config)]
    (j/start app)
    (info "word-count is up")
    app))

(comment
  (list-topics )
  (delete-topic! "test2")
  (create-topic! (make-topic-config "blabus"))
  (publish! (make-topic-config "bbb")  "blib")
  (map :value (get-records (make-topic-config "bbb")))
  
  (map :value (get-records (make-topic-config "input")))
  (map :value (get-records (make-topic-config "blabus")))
  
  )

