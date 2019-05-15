;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-15 23:22 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.core-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [problem_4_interview.handler :refer :all]
            [ring.mock.request :as mock]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

;; (deftest acceptance-test
  
;;   (testing "Test GET request to /hello?name={a-name} returns expected response"
;;     (let [response (app (-> (mock/request :get  "/api/plus?x=1&y=2")))
;;           body     (parse-body (:body response))]
;;       (is (= (:status response) 200))
;;       (is (= (:result body) 3)))))
