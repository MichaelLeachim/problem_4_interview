;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-11 19:32 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [jackdaw.client :as jc]
            [problem_4_interview.kafka :as kafka]
            [problem_4_interview.kafka-conf :as kafka-conf]
            [problem_4_interview.tools :as tools]))

;; ====================== App state ===================================

(def app-state (atom {:last-id 0 :filters {}}))

;; Filter example: 
;; {:topic "h"
;;  :q "a"
;;  :timestamp 1291231239
;;  :watch-fn (fn [item] (println item))
;;  :id 1
;;  :records [] }

;; ===================== App logic ====================================
(comment
  (map :value (kafka/list-records (kafka-conf/make-topic-config "input"))))

;; ~TESTED
(defn create-filter-watcher
  "Will take a filter from the App state and watch topic for the changes
   Will terminate, If unable to find a filter on the app state"
  [app-state filter-id]
  (let [filter-item (get-in @app-state [:filters filter-id])
        conf (kafka-conf/make-topic-config (:topic filter-item))
        consumer-group-id (str (:timestamp filter-item) "." (:topic filter-item))
        client-config  (kafka-conf/kafka-consumer-config consumer-group-id)]
    (with-open [client (jc/subscribed-consumer client-config
                                               [conf])]
      (loop [data (jc/poll client 200)]
        (doseq [item data]
          ((:watch-fn filter-item) item))
        ;; in case, the filter entry is still there
        (if (get-in @app-state [:filters filter-id])
          (recur (jc/poll client 200))
          ;; otherwise, stop processing
          nil)))))



;; TESTED
(defn match-item-fn
  [item creation-time q]
  (and (> (:timestamp item) creation-time)
       (clojure.string/includes?
        (clojure.string/lower-case (:value item))
        (clojure.string/lower-case q))))

(defn watch-fn
  [app-state item creation-time q last-id]
  (if (match-item-fn  item creation-time q)
                   (swap! app-state update-in [:filters last-id :records]
                          #(concat % [item]))
                   nil))

;; TESTED
(defn create-filter-item
  [app-state last-id topic q]
  (let [creation-time (System/currentTimeMillis)]
    {:topic topic
     :q  q
     :timestamp creation-time
     :watch-fn #(watch-fn app-state % creation-time q last-id)
     :id last-id
     :records  []}))

;; TESTED
(defn add-filter-to-the-state
  [app-state topic q]
  (let [{last-id :last-id  filters :filters} @app-state]
    (swap!
     app-state
     assoc
     :last-id (inc last-id)
     :filters
     (assoc filters last-id (create-filter-item app-state last-id topic q)))
    last-id))

(defn make-filter
  [app-state topic q]
  (let [filter-id (add-filter-to-the-state  app-state topic q)
        _ (future (create-filter-watcher app-state filter-id))]
    filter-id))

(defn delete-filter
  [app-state filter-id]
  (swap! app-state tools/dissoc-in [:filters filter-id]))

;; ===================== HTTP Interface ================================

;; TESTED
(defn make-app
  [app-state]
  (api
   (GET "/filter" []
        :query-params [{id :- Number -1}]
        :summary "Returns a list of current filters"
        (ok
         (if (not= id -1) 
           (for [{value :value} (get-in @app-state [:filters id :records])]
             value)
           (for [filter (vals (:filters @app-state))]
             (select-keys filter [:topic :q :timestamp :id])))))
   (POST "/filter" []
         :summary "Adds a new filter"
         :form-params [topic :- String, q :- String]
         (ok (do (let [id (make-filter app-state topic q)]
                   (select-keys (get-in @app-state [:filters id] )
                                [:q :id :timestamp :topic])))))
   (DELETE "/filter" []
           :summary "Deletes a filter"
           :query-params [id :- Number]
           (ok (do (delete-filter app-state id)
                   {:result "Successfully deleted"})))))

(def app (make-app app-state))
