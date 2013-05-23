(ns confil.extended-protocols
  (:require [confil.protocols :as protos]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.walk :as walk])
  (:import (com.typesafe.config Config
                                ConfigFactory)))

(extend-protocol protos/Configure

  java.lang.String
  (-config
    ([t]
     (cond
       (.endsWith t ".edn") (protos/-config (-> t io/resource slurp edn/read-string))
       :else (let [_ (System/setProperty "config.resource" t)
                   conf (ConfigFactory/load)
                   _ (System/clearProperty "config.resource")]
               conf)))
    ([t klass]
     (cast klass (protos/-config t))))

  clojure.lang.IPersistentMap
  (-config
    ([t]
     (ConfigFactory/parseMap (walk/stringify-keys t)))
    ([t klass]
     (cast klass (protos/-config t))))

  Config
  (-config
    ([t] (ConfigFactory/load t))
    ([t klass]
     (cast klass (protos/-config t))))

  nil
  (-config
    ([t]
     (ConfigFactory/empty))
    ([t klass]
     (cast klass (ConfigFactory/empty)))))

