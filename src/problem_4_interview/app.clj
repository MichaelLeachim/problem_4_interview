;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-12 15:14 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.app
  (:require
   [problem_4_interview.kafka :as kafka-tools]
   [clojure.core.async :as casync]))

;; {:id 1 :name "blab" :q "hello" :timestamp 123131231}

(def app-state
  (atom []))

(def input-chan (casync/chan))

(let [conf (tools/make-topic-config "input")
      consumer-group-id "input-reader"]
  (casync/go-loop
      [data (tools/list-records conf consumer-group-id)]
    (casync/<! (casync/timeout 200)) ;; poll interval
    (doseq [item data]
      (casync/>! input-chan item))
    (recur (tools/list-records conf consumer-group-id))))

(casync/go-loop []
  (println  (casync/<! input-chan))
  (recur))

(defn filter-processing-fn
  "Will take everything that is within a filter "
  [message]
  (doseq [filter app-state]))

(defn add-filter!
  [name topic]
  )


(tools/publish! (tools/make-topic-config "input") "blop")










tools/list-records















