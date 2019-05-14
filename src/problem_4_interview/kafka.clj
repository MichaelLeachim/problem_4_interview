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
   [jackdaw.admin :as ja]
   
   [problem_4_interview.kafka-conf :refer :all]))

(defn list-records
  ([topic-config]
   (list-records topic-config (str (java.util.UUID/randomUUID))))
  ([topic-config consumer-id]
   (let [client-config (kafka-consumer-config consumer-id)]
     (with-open [client (jc/subscribed-consumer client-config
                                                [topic-config])]
       (jc/poll client 200)))))

(defn publish!
  "Takes a topic config and record value, and (optionally) a key and
  parition number, and produces to a Kafka topic."
  ([topic-config value]
   (publish! topic-config nil value))
  ([topic-config key value]
   (with-open [client (jc/producer (kafka-producer-config) topic-config)]
     @(jc/produce! client topic-config key value))
   nil))

(defn list-topic-vals
  [topic]
  (map :value (list-records topic)))

(comment
  
  (map :value (list-records (make-topic-config "input") ))
  (map :value (list-records (make-topic-config "input_python")))
  (map :value (list-records (make-topic-config "input_java")))
  
  (map :value (list-records (make-topic-config "input_cap")))
  (map :value (list-records (make-topic-config "input_timestamp")))
  
  (delete-topic! (make-topic-config "java"))
  (create-topic! (make-topic-config "input"))
  
  (publish! (make-topic-config "input")  "python")
  
  (map :value (get-records (make-topic-config "bbb")))
  (defn mapper  [key val]
    (clojure.string/upper-case val))
  
  (list-topic-vals (make-topic-config "input"))
  (list-topic-vals (make-topic-config "input"))
  (list-topic-vals (make-topic-config "java"))
  (list-topic-vals (make-topic-config "uppercase"))
  (publish! (make-topic-config "input") "python33")
  (delete-topic! (make-topic-config "java"))
  (j/close java-filter)
  
  (map :value (list-records (make-topic-config "input") 200 ))
  
  
  )


