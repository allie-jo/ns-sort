(ns ns-sort-test
  (:require [clojure.test :refer [deftest is]]
            [ns-sort.core :as ns-sort]))

(deftest update-code-test
  (is (= (ns-sort/update-code (slurp "test/data/unsorted.clj-test"))
         (slurp "test/data/sorted.clj-test"))))

(deftest ns-permutations-test
  (let [unchanged? (fn [s] (= s (ns-sort/update-code s)))]
    (is (unchanged? "(ns test-ns)"))
    (is (unchanged? "(ns test-ns \"docstring\")"))
    (is (unchanged? "(ns test-ns (:require [clojure.string :as str]))"))
    (is (unchanged? "(ns test-ns (:import (java.io File)))"))
    (is (unchanged?
         "(ns test-ns (:require [clojure.string :as str]) (:import (java.io File)))"))))

(deftest keywords-in-body-test
  (ns-sort/update-code
   "(ns test-ns (:require [clojure.string :as str])) (println :import)"))
