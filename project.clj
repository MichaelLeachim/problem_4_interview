 (defproject problem_4_interview "0.1.0-SNAPSHOT"
   :description "Test task for the interview"
   :dependencies [[org.clojure/clojure "1.10.0"]
                  
                  [metosin/compojure-api "1.1.11"]
                  
                  [fundingcircle/jackdaw "0.6.4"]
                  [org.clojure/core.async "0.4.490"]]
   :ring {:handler problem_4_interview.handler/app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                   [cheshire "5.5.0"]
                                   [ring/ring-mock "0.3.0"]
                                   [org.apache.kafka/kafka-streams-test-utils "2.1.0"]
                                   [org.apache.kafka/kafka-clients "2.1.0" :classifier "test"]]
                    :plugins [[lein-ring "0.12.0"]
                              [com.jakemccrary/lein-test-refresh "0.24.1"]]}})
