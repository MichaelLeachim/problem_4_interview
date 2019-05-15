;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-12 15:14 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.app
  
  (:require
   [jackdaw.streams :as j]
   [problem_4_interview.kafka :as kafka]
   [problem_4_interview.kafka-topics :as kafka-topics]
   [problem_4_interview.tools :as tools]
   [problem_4_interview.kafka-streams :as kafka-streams]))

;; Filter name:     <app_prefix><m|f><timestamp>
;; App state store: <app_prefix><m>app-state

(def app-state (atom {:last-id 0 :filters {}}))

;; ========== Immutable state modifiers (Similar to events in ReFrame) =========

(defn- make-filter-topology
  [app-id topic q timestamp]
  (let [ql      (clojure.string/lower-case q)
        m-topic (str  app-id "m" timestamp)
        f-topic (str  app-id "f" timestamp)]
    [[:map-with-timestamp topic m-topic
      (fn [ts k v]
        [k [ts v]])]
     [:filter m-topic f-topic
      (fn [k [ts v]]
        (and (>= ts timestamp)
             (clojure.string/includes? (clojure.string/lower-case v)
                                       ql)))]]))

(defn add-filter-event
  [{last-id :last-id  filters :filters :as app-state}  app-id timestamp topic q]
  (-> app-state
      (assoc :last-id (inc last-id))
      (assoc-in  [:filters last-id]
                 {:id last-id
                  :topic topic
                  :ts timestamp
                  :q q
                  :topology (make-filter-topology app-id topic q timestamp)})))

(defn remove-filter-event
  [{filters :filters  :as app-state} topic-id]
  (assoc
   app-state
   :filters 
   (dissoc filters topic-id)))

(defn list-filters-sub
  [{filters :filters}]
  (for [filter filters]
    (select-keys filter [:id :topic :q :timestamp])))

(defn with-state
  [wrap-fn & rest]
  (let [old-app-state @app-state
        new-app-state (apply wrap-fn old-app-state rest)]
    (apply
     kafka-streams/run-kstreams
     (reduce concat (map :topology (vals (:filters new-app-state)))))
    (reset! app-state new-app-state)))

