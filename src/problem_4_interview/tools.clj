;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-14 23:53 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.tools)

(defn ->cur-time
  [] (System/currentTimeMillis))

(defn str->int
  [in]
  (. Integer parseInt  int))



