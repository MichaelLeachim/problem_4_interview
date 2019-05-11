 (defproject problem_4_interview "0.1.0-SNAPSHOT"
   :description "FIXME: write description"
   :dependencies [[org.clojure/clojure "1.10.0"]
                  
                  [metosin/compojure-api "1.1.11"]

                  [fundingcircle/jackdaw "0.6.4"]

                  
                  [org.clojure/tools.logging        "0.4.0"]
                  [ch.qos.logback/logback-classic   "1.2.3"]
                  [org.slf4j/log4j-over-slf4j       "1.7.25"]
                  [org.apache.kafka/kafka-clients   "1.0.0"]
                  [org.apache.curator/curator-test  "2.8.0"]
                  [org.apache.kafka/kafka_2.11      "1.0.0" :exclusions  [org.slf4j/slf4j-log4j12
                                                                          log4j/log4j]]
                  [me.raynes/fs  "1.4.6"]]
   :ring {:handler problem_4_interview.handler/app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [cheshire "5.5.0"]
                                  [ring/ring-mock "0.3.0"]]
                   :plugins [[lein-ring "0.12.0"]]}})
