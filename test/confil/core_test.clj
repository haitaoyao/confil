(ns confil.core-test
  (:require [clojure.test :refer :all]
            [confil.core :as confil])
  (:import (com.typesafe.config Config)
           (com.typesafe.config.impl SimpleConfig)))

(deftest safe-config-test
  (testing "with no args, defaults to application.conf"
    (is (= 7 (:foo.bar (confil/safe-config)))))
  (testing "with a valid resource name, has correct config"
    (is (= 7 (:foo.bar (confil/safe-config "application.conf")))))
  (testing "with an invalid resource name, exception is thrown"
    (is (thrown? java.io.FileNotFoundException (confil/safe-config "blarg.conf"))))
  (testing "with an edn resource, has correct config"
    (is (= [1 2 3] (:a (confil/safe-config "application.edn")))))
  (testing "with a map, returns it back as a config"
    (is (= 7 (:foo.bar (confil/safe-config {:foo.bar 7})))))
  (testing "with nil has empty config"
    (is (= {} (confil/safe-config nil))))
  (testing "with a valid resource name and Config class, returns back correct config class"
    (is (= SimpleConfig (type (confil/->config (confil/safe-config "application.conf" SimpleConfig)))))))

(deftest ->config-test
  (testing "returns back a Config object from the created config"
    (is (instance? Config (confil/->config (confil/safe-config))))))

