;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leahcim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2019-05-11 22:07 <thereisnodotcollective@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@

(ns problem_4_interview.kafka-helpers
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [me.raynes.fs :as fs])
  (:import [org.apache.curator.test TestingServer]
           [kafka.server KafkaConfig KafkaServerStartable]))

;; TODO: figure out how to start up
;;       send several messages
;;       receive those messages
;;       use streams API to make a words filter: consume from the topic <main>, post to topic filter
;;       use admin API to create and delete topics

(defn start-zookeeper [port]
  (fs/delete-dir "/tmp/zk")
  (let [zk (TestingServer. port (io/file "/tmp/zk"))]
    (log/info "zk started")
    zk))

(defn start-kafka-server [zk-address]
  (let [config (KafkaConfig. {"zookeeper.connect"                 zk-address
                              "listeners"                        "PLAINTEXT://127.0.0.1:9092"
                              "auto.create.topics.enable"        true
                              "offsets.topic.replication.factor" (short 1)
                              "offsets.topic.num.partitions"     (int 1)})
        kafka  (KafkaServerStartable. config)]
    (.startup kafka)
    (log/info "kafka started")
    kafka))

(comment
  (def zookeeper  (start-zookeeper 2181))
  (def kafka  (start-kafka-server "localhost:2181"))
  (.stop kafka)
  
  
 )

