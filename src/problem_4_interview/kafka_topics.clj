;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-14 18:56 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.kafka-topics
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [jackdaw.serdes.edn :as jse]
   [jackdaw.serdes.resolver :as resolver]
   [jackdaw.client :as jc]
   [jackdaw.streams :as j]
   [jackdaw.admin :as ja]
   
   [problem_4_interview.kafka-conf :refer :all]))

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
(comment
  (list-topics)
  )
