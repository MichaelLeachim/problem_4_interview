;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-14 18:54 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

;; Inspired by https://github.com/FundingCircle/jackdaw/tree/master/examples/dev

(ns problem_4_interview.kafka-conf
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [jackdaw.serdes.edn :as jse]
   [jackdaw.serdes.resolver :as resolver]
   [jackdaw.client :as jc]
   [jackdaw.streams :as j]
   [jackdaw.admin :as ja]))

(defn get-env
  [param default]
  (get (System/getenv) param default))

(def app-id
  (get-env "APPLICATION_ID" "problem_4_interview"))

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
   "enable.auto.commit" "false"
   ;; "true"
   })

(def application-config
  {"application.id"            app-id 
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

