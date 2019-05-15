;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-15 16:49 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.kafka-streams-test
  (:require
   [problem_4_interview.kafka-topics :as kafka-topics]
   [problem_4_interview.kafka-streams :as kafka-streams]
   [problem_4_interview.kafka-conf :refer :all]
   [problem_4_interview.kafka :as kafka]

   [clojure.test :refer :all]))

(deftest test-run-streams-workage
  
  (testing "Acceptance test on streams"
    
    ;; delete stale topics
    (doseq [item (filter #(clojure.string/starts-with? % "test_topic") (map :topic-name (kafka-topics/list-topics)))]
      (kafka-topics/delete-topic! (make-topic-config item)))
    
    ;; ;; create input topic
    (kafka-topics/create-topic-if-not-exists! (make-topic-config "test_topic_input"))
    
    ;; create kstream runner
    (kafka-streams/run-kstreams
     [:filter "test_topic_input" "test_topic_input_python" (fn [k v] (= v "python"))]
     [:filter "test_topic_input" "test_topic_input_java" (fn [k v] (= v "java"))]
     [:map-values    "test_topic_input" "test_topic_input_cap"  (fn [v] (clojure.string/capitalize v))]
     [:map-with-timestamp    "test_topic_input" "test_topic_input_timestamp"
      (fn [ts k v]  [k {:ts ts :v v}])]
     [:map-values    "test_topic_input" "test_topic_input_dict" (fn [v]  {:hello v})])
    
    ;; publish some data
    (let [tti (make-topic-config "test_topic_input")]
      (kafka/publish! tti  "python")
      (kafka/publish! tti  "java")
      (kafka/publish! tti  "blab")
      (kafka/publish! tti  "blip")
      (kafka/publish! tti  "python"))
    (Thread/sleep 2000)
    ;; check results
    (is (=  (map :value (kafka/list-records (make-topic-config "test_topic_input"))) (list "python" "java" "blab" "blip" "python")))
    (is (=  (map :value (kafka/list-records (make-topic-config "test_topic_input_python"))) (list "python" "python")))
    (is (=  (map :value (kafka/list-records (make-topic-config "test_topic_input_java"))) (list "java")))
    (is (=  (map :value (kafka/list-records (make-topic-config "test_topic_input_java_cap"))) (list "java")))
    (is (=  (map :value (kafka/list-records (make-topic-config "test_topic_input_timestamp"))) (list "java")))
    ))

(comment
   (def streams-builder (jmock/streams-builder))
   (def test-driver (jmock/streams-builder->test-driver streams-builder)))




   



