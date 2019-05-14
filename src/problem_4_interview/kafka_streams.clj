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

(defonce ^{:private true} prev-app (atom nil))


(defn- create-topic-if-not-exists!
  "Takes a topic config and creates a Kafka topic."
  [topic-config]
  (with-open [client (ja/->AdminClient (kafka-admin-client-config))]
    (if-not (ja/topic-exists? client topic-config)
      (ja/create-topics! client [topic-config]))))

;; =========================== Topology DSL ===============================

;; To keep our sanity in check, here is the proposal
;; We create a DSL for the Kafka streams API

;; We are also storing the previous instance of Kafka streams API
;; in the atom. Every call to the run-kstreams will reset the topology

;; This function will take a topology:
;;   [:map    "input" "input_with_date" (fn [k v] ))]
;;   [:filter "java" "python" (fn [k v] (clojure.string/c))]
;; And produce a kstream app: 
;;   It will stop the previous isntance of a kstream app, if it exists


(defn- filter-fn
  [from-topics left right work-fn]
  (->
   (j/filter
    (get from-topics left)
    (fn [[k v]]
      (work-fn k v)))
   (j/to (make-topic-config right))))

(defn- filter-not-fn
  [from-topics left right work-fn]
  (->
   (j/filter-not
    (get from-topics left)
    (fn [[k v]]
      (work-fn k v)))
   (j/to (make-topic-config right))))

(defn- map-values-fn
  [from-topics left right work-fn]
  (->
   (j/map-values
    (get from-topics left)
    (fn [val]
      (work-fn val)))
   (j/to (make-topic-config right))))

(defn- topics-str->kstreams
  ;; (topics-str->kstreams (j/streams-builder) (list "hello" "world"))
  [builder in]
  (into
   {}
   (for [topic (distinct in)]
     [topic (j/kstream builder (make-topic-config topic))])))

;; Topology builder

(defn- build-topology
  [topology]
  (let [builder (j/streams-builder)
        from-topics (topics-str->kstreams builder (for [[_ in _ _]  topology] in))
        to-topics   (topics-str->kstreams builder (for [[_ _  out _] topology] out))]
    
    (doseq [[predicate left right work-fn] topology]
      (condp = predicate
        :map-values
        (map-values-fn from-topics left right work-fn)
        :filter
        (filter-fn from-topics left right work-fn)
        :filter-not
        (filter-not-fn from-topics left right work-fn)
        (throw (Exception. (str "Wrong topology: " predicate " is undefined")))))
    
    builder))

(defn run-kstreams
  [& topology]
  (if @prev-app (j/close @prev-app))
  (reset! prev-app
          (let [app (j/kafka-streams (build-topology topology) application-config)]
            (j/start app)
            app)))

(comment
  
  (run-kstreams
   [:filter "input" "input_python" (fn [k v] (= v "python"))]
   [:filter "input" "input_java" (fn [k v] (= v "java"))]
   [:map-values    "input" "input_cap"  (fn [v] (clojure.string/capitalize v))]
   [:map-values    "input" "input_dict" (fn [v]  {:hello v})])
  
  (map :value (list-records (make-topic-config "input") ))
  (map :value (list-records (make-topic-config "input_python")))
  (map :value (list-records (make-topic-config "input_java")))
  
  (map :value (list-records (make-topic-config "input_cap")))
  (map :value (list-records (make-topic-config "input_dict")))
  
  (publish! (make-topic-config "input")  "python")  

  )
