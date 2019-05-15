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
            [problem_4_interview.app :as kafka-app]))

(s/defschema Filter
  {:topic s/Str
   :q s/Str
   :timestamp s/Num
   :id s/Num})

(def app
  (api
    (context "/api" []
      (GET "/filter" []
        :return {:result Long}
        :query-params [x :- Long, y :- Long]
        :summary "Returns a list of filters that are currently working"
        (ok {:result 12}))
      
      (GET "/filter" []
        :return {:result Long}
        :query-params [x :- Long, y :- Long]
        :summary "adds two numbers together"
        (ok {:result (+ x y)}))
      
      ;; (POST "/filter" []
      ;;   :return Pizza
      ;;   :body [pizza Pizza]
      ;;   :summary "echoes a Pizza"
      ;;   (ok pizza))
      )))
