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

(defn fallbacks
  ""
  [config-fn & confs]
  (let [conf-objs (map (fn [[path-or-obj ConfigImplementer]]
                         (if ConfigImplementer
                           (config-fn path-or-obj ConfigImplementer)
                           (config-fn path-or-obj))) confs)]
    (reduce (fn [[final-conf new-conf]] (.withFallback new-conf final-conf)) conf-objs)))

(defn safe-configs
  "Create a new config by sequentially adding configs
  as fallbacks.
  e.g. (-> (safe-config 'one.conf') (.withFallback (safe-config 'two.conf')) ...)
  If the config is a vector, it's treated as a [path-or-obj ConfigImplementer] pair"
  [& confs]
  (apply fallbacks safe-config confs))


(comment
  (.exists (io/reader "http://www.google.com"))
  (def c (safe-config))
  (first  (.entrySet (->config c)))
  (:foo.baz c)
  )

;; Interface called Config
;; our config/config returns an object
;;
;; Config conf = ConfigFactory.load();
;; int bar1 = conf.getInt("foo.bar");
;; Config foo = conf.getConfig("foo");
;; int bar2 = foo.getInt("bar");
;;

;(confil/config) => "application.conf" within resources, standard Typesafe Config parser
;(confil/config "some/path/to/a.conf") => within resources, standard Typesafe Config parser
;(confil/config "/etc/my-app/application.conf" SaksConfig) => {} (.get % "some-attribute")
;
;(confil/config ...) is a protocol that dispatches on:
; * strings (file/url/etc)
; * hashmaps (config-like objects)
; * Config objects (no-op)
; * nil (empty Config)
;
;each returns Clojure hash-map, that fulfills the Config interface.
;
;"*.conf" => ConfigParser
;"*.edn" => read-string-edn and then create the Config object from that

