(ns confil.core
  (:require [confil.protocols :as protos]
            [confil.extended-protocols]
            [clojure.java.io :as io])
  (:import (com.typesafe.config Config
                                ConfigValue)
           (java.io FileNotFoundException)))

(defn config->hash-map
  "Returns a hash-map of the Typesafe Config.
  The original Config is stored within the meta data under `::config`."
  [^Config config]
  (with-meta (reduce (fn [accum-map [k ^ConfigValue config-value]]
                       (assoc accum-map (keyword k) (.unwrapped config-value))) {} (.entrySet config))
             {::config config}))

(defn ->config
  "Returns the Typesafe Config for a given
  config map that was created with `config->hash-map`."
  [config-map]
  (::config (meta config-map)))

(defn config
  "Returns back a hash-map of the configuration,
  supporting the Typesafe Config "
  ([]
   (config "application.conf"))
  ([path-or-obj]
   (config->hash-map (protos/-config path-or-obj)))
  ([path-or-obj ConfigImplementer]
   (config->hash-map (protos/-config path-or-obj ConfigImplementer))))

(defn valid-resource?!
  "Throw an exception when an invalid resource path is used,
  otherwise true."
  [resource]
  (when (string? resource)
    (or (io/resource resource)
        (throw (FileNotFoundException.
                 (str resource " was not found on the classpath; not a resource.")))))
  true)

(defn safe-config
  "Like `config` but will throw a FileNotFound exception
  if your config resource can't be located."
  ([]
   (safe-config "application.conf"))
  ([path-or-obj]
   (and (valid-resource?! path-or-obj)
        (config path-or-obj)))
  ([path-or-obj ConfigImplementer]
   (and (valid-resource?! path-or-obj)
        (config path-or-obj ConfigImplementer))))

(defn typesafe-config? [possible-config]
  (instance? Config possible-config))

(defn fallbacks
  "Set a series of Configs to fallback.
  Optionally cast the config by placing it in a vector: [config ConfigImplementerClass]"
  [config-fn & confs]
  (let [conf-objs (map (fn [[path-or-obj ConfigImplementer :as config-entry]]
                         (if (class? ConfigImplementer)
                           (config-fn path-or-obj ConfigImplementer)
                           (config-fn config-entry))) confs)]
    (config->hash-map
      (reduce (fn [final-conf new-conf] (.withFallback (->config new-conf) final-conf))
              (->config (first conf-objs))
              (rest conf-objs)))))

(defn safe-configs
  "Create a new config by sequentially adding configs
  as fallbacks.
  e.g. (-> (safe-config 'one.conf') (.withFallback (safe-config 'two.conf')) ...)
  If the config is a vector, it's treated as a [path-or-obj ConfigImplementer] pair"
  [& confs]
  (apply fallbacks (conj confs safe-config)))


(comment
  (.exists (io/reader "http://www.google.com"))
  (def c (safe-config))
  (first  (.entrySet (->config c)))
  (:foo.baz c)
  )

