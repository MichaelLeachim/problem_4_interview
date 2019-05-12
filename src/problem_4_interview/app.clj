(ns problem_4_interview.app
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.tools.logging :refer [info]]
   [jackdaw.serdes.edn :as jse]
   [jackdaw.serdes.resolver :as resolver]
   [jackdaw.client :as jc]
   [jackdaw.streams :as j]
   [jackdaw.admin :as ja]))


(def app-config
  "Returns the application config."
  {"application.id"            "word-count"
   "bootstrap.servers"         bootstrap-servers
   "default.key.serde"         "jackdaw.serdes.EdnSerde"
   "default.value.serde"       "jackdaw.serdes.EdnSerde"
   "cache.max.bytes.buffering" "0"})

(defn split-lines
  "Takes an input string and returns a list of words with the
  whitespace removed."
  [input-string]
  (str/split (str/lower-case input-string) #"\W+"))

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

(defn start-app
  "Starts the stream processing application."
  [topic-metadata app-config]
  (let [builder (j/streams-builder)
        topology ((topology-builder topic-metadata) builder)
        app (j/kafka-streams topology app-config)]
    (j/start app)
    (info "word-count is up")
    app))

(defn get-env [k default]
  (get (System/getenv) k default))

(defn- kafka-producer-config
  []
  {"bootstrap.servers" bootstrap-servers})

(defn- kafka-consumer-config
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

(comment
  (start-app topic-metadata app-config)
  
  (def app  (start-app))
  (map println (:input topic-metadata))
  (publish (:input topic-metadata) nil "all streams lead to kafka")
     

  
  

  

  
  )








