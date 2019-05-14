;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-12 15:14 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.app
  (:require
   [problem_4_interview.kafka :as kafka]
   [problem_4_interview.kafka-topics :as topics-kafka]
   [problem_4_interview.tools :as tools]))

;; (Topic A, Topic B, Topic C) -> TopicAggregator
;; TopicAggregator -> (Filter A, Filter B, Filter C)
;; Filter name: prefix_<from_topic>_query_<timestamp>
;; 

(defonce topology
  (atom []))


(defn make-topic-aggregator
  []
  
  
  )
[:map-values "topic-a" "topic-aggregator" {:from "topic-a" :message "blab" :time "123123123"}]
[:map-values "topic-b" "stamped_topic"]
[:map-values "topic-k" "stamped_topic"]
[:map-values "topic-n" "stamped_topic"]




(defn load-topology
  (for [item (filter (clojure.string/starts-with? % "filter_") (map :topic-name (topics-kafka/list-topics)))]
    (let [[_ tstamp topic filters-on] (clojure.string/split item "_")]
      (tools/str->int tstamp)
      [:filter (str topic "_") item (fn [k v] (clojure.string/))]
      
      
      
      
      
      )
    
    
    
    

    )
  )




;; (defn filter-processing-fn
;;   "Will take everything that is within a filter "
;;   [message]
;;   (doseq [filter app-state]))

;; (defn add-filter!
;;   [name topic]
;;   )













