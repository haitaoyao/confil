(defproject ohpauleez/confil "0.1.0-beta5"
  :description "Consuming Typesafe's Config from Clojure"
  :url "https://github.com/ohpauleez/confil"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.typesafe/config "1.0.1"]
                 ;[clj-time "0.5.1"]
                 ]
  :profiles {:dev {:resource-paths ["test/resources"]}})

