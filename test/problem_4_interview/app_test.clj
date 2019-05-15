;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-15 23:19 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.app-test
  (:require [problem_4_interview.app :as main-app]
            [problem_4_interview.kafka-topics :as kafka-topics]
            [clojure.test :refer :all]))

;; (main-app/with-state
;;  main-app/add-filter-event
;;  "testing_app"
;;  1557954295964
;;  "input"
;;   "bbb")

;; (:topology
;;  (get 
;;       (:filters
;;        (main-app/add-filter-event
;;         {:last-id 0 :filters {}}
;;         "testing_app"
;;         1557954295964
;;         "input"
;;         "bbb")) 0))

;; (deftest acceptance-tests
;;   (let [app-id "app_testing"]
;;     (testing "Test add a filter "
;;       (let [response ()
;;             body     (parse-body (:body response))]
;;         (is (= (:status response) 200))
;;         (is (= (:result body) 3))))))


