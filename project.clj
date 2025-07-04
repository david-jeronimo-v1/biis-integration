(defproject biis-integration "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [clj-http "3.13.0"]
                 [org.clojure/data.json "2.5.1"]
                 [cheshire "6.0.0"]
                 [clojure.java-time "1.4.3"]
                 [org.apache.logging.log4j/log4j-api "2.11.0"]
                 [org.apache.logging.log4j/log4j-core "2.11.0"]
                 [org.apache.logging.log4j/log4j-1.2-api "2.11.0"]]
  :repl-options {:init-ns biis-integration.core}
  :main biis-integration.core)
