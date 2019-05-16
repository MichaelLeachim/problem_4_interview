;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-15 23:22 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.core-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]

            [problem_4_interview.handler :as handler]
            [problem_4_interview.kafka :as kafka]
            [problem_4_interview.kafka-conf :as kafka-conf]))

(def mock-state
  {:last-id 0
   :filters {}})


(deftest testing-logic
  (testing "Matching item function"
    (is (=
         (handler/match-item-fn
          {:timestamp (System/currentTimeMillis)
           :value "Hello world"}
          1557963118390 "HELLO")) true)
    (is (=
         (handler/match-item-fn
          {:timestamp (System/currentTimeMillis)
           :value "Hello world"}
          1557963118390 "HLLO")) false))
  (testing "Create filter item"
    (is (=  (dissoc  (handler/create-filter-item (atom mock-state) 0 "input" "hello")
                     :watch-fn
                     :timestamp)
            {:topic "input" :q "hello"  :id 0, :records []})))
  (testing "Watch fn"
    (let [state (atom (assoc-in mock-state [:filters 0] {:records []}))]
      (handler/watch-fn state
                        {:timestamp (System/currentTimeMillis)
                         :value "Hello world"}
                        1557963118390 "HeLLO" 0)
      (is (=  (count (get-in @state [:filters 0 :records ])) 1))))
  
  (testing "Add filter to the state"
    (let [state (atom mock-state)]
      (handler/add-filter-to-the-state
       state "input" "python")
      (is (= (:last-id @state) 1))
      (is (= (dissoc (get-in @state [:filters 0]) :watch-fn :timestamp)
             {:topic "input" :q "python"  :id 0, :records []}))))
  
  
  (testing "Making filter"
    (let [app-state (atom mock-state)]
      (handler/make-filter app-state "input" "python")
      (handler/make-filter app-state "input" "java")
      (kafka/publish! (kafka-conf/make-topic-config "input") "python")
      (kafka/publish! (kafka-conf/make-topic-config "input") "javascript")
      (kafka/publish! (kafka-conf/make-topic-config "input") "hello")
      (kafka/publish! (kafka-conf/make-topic-config "input") "java")
      (Thread/sleep 500)
      (is (=  (count (get-in @app-state [:filters])) 2))
      (is (=  (count (get-in @app-state [:filters 1 :records])) 2))
      (is (=  (count (get-in @app-state [:filters 0 :records])) 1)))))


(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(deftest http-interface-test
  (testing "Test GET request to return a set of filters"
    (let [app-state (atom mock-state)
          _ (handler/add-filter-to-the-state app-state "input" "python")
          _ (handler/add-filter-to-the-state app-state "input" "java")
          _ (handler/add-filter-to-the-state app-state "input" "clojure")
          response ((handler/make-app app-state) (-> (mock/request :get "/filter")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (=  (count body) 3))))
  
  (testing "Test GET request to return a result of the working filter"
    (let [app-state (atom mock-state)
          _ (handler/add-filter-to-the-state app-state "input" "python")
          _ (swap! app-state assoc-in [:filters 0 :records]
                   [{:value "hello"}
                    {:value "world"}])
          
          response ((handler/make-app app-state) (-> (mock/request :get "/filter" {:id 0})))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= body
             (list "hello" "world")))))
  
  (testing "Test post request to add a new filter"
    (let [app-state (atom mock-state)
          response ((handler/make-app app-state) (-> (mock/request :post "/filter"
                                                                   {:topic "hello"
                                                                    :q "world"})))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (dissoc body :timestamp)
             {:q "world" :id 0 :topic "hello"}))))

  (testing "Test delete request to remove a filter"
    (let [app-state (atom mock-state)
          _ (handler/add-filter-to-the-state app-state "input" "python")
          response ((handler/make-app app-state) (-> (mock/request :delete "/filter"
                                                                    {:id 2})))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= body {:result "Successfully deleted"}))
      (is (= (count (:filters app-state)) 0)))))
