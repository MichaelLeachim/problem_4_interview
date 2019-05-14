;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-14 18:53 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

;; Inspired by https://github.com/FundingCircle/jackdaw/tree/master/examples/dev

(ns problem_4_interview.kafka-streams
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [jackdaw.serdes.edn :as jse]
   [jackdaw.serdes.resolver :as resolver]
   [jackdaw.client :as jc]
   [jackdaw.streams :as j]
   [jackdaw.admin :as ja]
   [problem_4_interview.kafka-conf :refer :all]))

;; ======================= Streams =====================================

(defn- create-topic-if-not-exists!
  "Takes a topic config and creates a Kafka topic."
  [topic-config]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (if-not (ja/topic-exists? client topic-config)
      (ja/create-topics! client [topic-config]))))

(defn- build-filter-fn
  [builder from-topic to-topic filter-fn]
  (let [input-stream   (j/kstream builder from-topic)
        filter-fn
        (j/filter input-stream
                  (fn [[k v]]
                    (filter-fn k v)))
        _ (j/to filter-fn to-topic)]
    builder))


(defn- build-map-fn
  [builder from-topic to-topic filter-fn]
  (let [input-stream   (j/kstream builder from-topic)
        filter-fn
        (j/map input-stream
                  (fn [[k v]]
                    (filter-fn k v)))
        _ (j/to filter-fn to-topic)]
    builder))

(defn create-filter-stream!
  [from-topic to-topic filter-fn]
  (let [builder (j/streams-builder)
        _ (create-topic-if-not-exists! to-topic)
        _ (build-filter-fn builder from-topic to-topic filter-fn)
        app (j/kafka-streams builder application-config)
        _ (j/start app)]
    app))

(defn create-map-stream!
  [from-topic to-topic filter-fn]
  (let [builder (j/streams-builder)
        _ (create-topic-if-not-exists! to-topic)
        _ (build-filter-fn builder from-topic to-topic filter-fn)
        app (j/kafka-streams builder application-config)
        _ (j/start app)]
    app))

(comment
  
  (def java-filter
    (create-filter-stream!
     (make-topic-config "input")
     (make-topic-config "java")
     (fn [key val]
       (clojure.string/includes? (clojure.string/lower-case val) "java"))))
  
  (def python-filter
    (create-filter-stream!
     (make-topic-config "input")
     (make-topic-config "java")
     (fn [key val]
       (clojure.string/includes? (clojure.string/lower-case val) "python"))))
  
  (j/close java-filter)
  

  )
