(ns ns-sort-test
  (:require [clojure.test :refer [deftest is]]
            [ns-sort.core :as ns-sort]))

;; (deftest format-ns-test
;;   (is (= (ns-sort/format-ns '(ns
;;                                leiningen.ns-sort
;;                                (:require [clojure.java.io :as io]
;;                                          [clojure.string :as string])
;;                                (:import (java.io File))))
;;          "(ns leiningen.ns-sort
;;   (:require [clojure.java.io :as io]
;;             [clojure.string :as string])
;;   (:import (java.io File)))")))

;; (deftest sort-requires-test
;;   (is (= (ns-sort/sort-requires 'my-server '[[schema.core :as s]
;;                                              [compojure.api.sweet :refer [GET POST]]
;;                                              [ring.util.http-response :refer [content-type ok]]
;;                                              [my-server.db.api.anomalies :as db-anomalies]
;;                                              [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
;;                                              [ring.swagger.json-schema :refer [describe]]])

;;          '[[my-server.db.api.anomalies :as db-anomalies]
;;            [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
;;            [compojure.api.sweet :refer [GET POST]]
;;            [ring.swagger.json-schema :refer [describe]]
;;            [ring.util.http-response :refer [content-type ok]]
;;            [schema.core :as s]])))

;; (deftest update-ns-test
;;   (is (= (ns-sort/update-ns "(ns my-server
;;   (:require [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
;;             [compojure.api.sweet :refer [GET POST]]
;;             [ring.util.http-response :refer [content-type ok]]
;;             [ring.swagger.json-schema :refer [describe]]
;;             [my-server.db.api.anomalies :as db-anomalies]
;;             [schema.core :as s]))")

;;          "(ns my-server
;;   (:require [my-server.db.api.anomalies :as db-anomalies]
;;             [my-server.web.handlers.schema.common :refer [ClickhouseFieldDef]]
;;             [compojure.api.sweet :refer [GET POST]]
;;             [ring.swagger.json-schema :refer [describe]]
;;             [ring.util.http-response :refer [content-type ok]]
;;             [schema.core :as s]))")))

(deftest update-code-test
  (is (= (ns-sort/update-code (slurp "test/data/unsorted.clj-test"))
         (slurp "test/data/sorted.clj-test"))))

(deftest ns-permutations-test
  (let [unchanged? (fn [s] (= s (ns-sort/update-code s)))]
    (is (unchanged? "(ns test-ns)"))
    (is (unchanged? "(ns test-ns \"docstring\")"))
    (is (unchanged? "(ns test-ns (:require [clojure.string :as str]))"))
    (is (unchanged? "(ns test-ns (:import (java.io File)))"))
    (is (unchanged? "(ns test-ns (:require [clojure.string :as str]) (:import (java.io File)))"))))
