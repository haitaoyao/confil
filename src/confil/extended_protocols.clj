(ns confil.extended-protocols
  (:require [confil.protocols :as protos]
            [clojure.edn :as edn])
  (:import (com.typesafe.config Config
                                ConfigFactory)
           (com.typesafe.config.impl SimpleConfig)))

(extend-protocol protos/Configure

  java.lang.String
  (-config
    ([t]
     (cond
       (.endsWith t ".edn") (protos/-config (edn/read-string (slurp t)))
       :else (let [_ (System/setProperty "config.resource" t)
                   conf (ConfigFactory/load)
                   _ (System/clearProperty "config.resource")]
               conf)))
    ([t klass]
     (cast klass (protos/-config t))))

  clojure.lang.IPersistentMap
  (-config
    ([t])
    ([t klass]))

  Config
  (-config
    ([t] t)
    ([t klass]
     (cast klass (ConfigFactory/load t))))

  nil
  (-config
    ([t]
     (ConfigFactory/empty))
    ([t klass]
     (cast klass (ConfigFactory/empty)))))

