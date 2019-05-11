(ns problem_4_interview.kafka-helpers
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [me.raynes.fs :as fs])
  (:import [org.apache.curator.test TestingServer]
           [kafka.server KafkaConfig KafkaServerStartable]))

(defn start-zookeeper [port]
  (fs/delete-dir "/tmp/zk")
  (let [zk (TestingServer. port (io/file "/tmp/zk"))]
    (log/info "zk started")
    zk))

(defn start-kafka-server [zk-address]
  (let [config (KafkaConfig. {"zookeeper.connect"                zk-address
                              "listeners"                        "PLAINTEXT://127.0.0.1:9092"
                              "auto.create.topics.enable"        true
                              "offsets.topic.replication.factor" (short 1)
                              "offsets.topic.num.partitions"     (int 1)})
        kafka  (KafkaServerStartable. config)]
    (.startup kafka)
    (log/info "kafka started")
    kafka))
