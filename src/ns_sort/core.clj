(ns ns-sort.core
  (:gen-class)
  (:require [clojure.string :as str]
            [rewrite-clj.parser :as p]
            [rewrite-clj.zip :as z])
  (:import (java.io File)))

(defn ^:private get-sortable
  "Given a zloc, get something out of it that is sortable.

  ORIGINALLY FROM: zprint.rewrite"
  [zloc]
  (if (z/seq? zloc)
    (-> zloc
        z/down
        z/string)
    (z/string zloc)))

(defn ^:private sort-val
  "Sort the everything in the seq to the right of zloc.

  ORIGINALLY FROM: zprint.rewrite"
  [zloc sort-fn]
  (let [;; zloc (z/next zloc)
        dep-seq
        (loop [nloc zloc out []] (if nloc (recur (z/right nloc) (conj out nloc)) out))

        sorted-seq (sort-by sort-fn dep-seq)]
    (loop [nloc zloc
           new-loc sorted-seq
           last-loc nil]
      (if new-loc
        (let [new-z (first new-loc)
              ; rewrite-cljs doesn't handle z/node for :uneval
              ; so we will get an :uneval node a different way
              new-node (if (= (z/tag new-z) :uneval)
                         (p/parse-string (z/string new-z))
                         (z/node new-z))
              ; use clojure.zip for cljs, since the z/replace has
              ; a built-in coerce, which doesn't work for an :uneval
              ;; #?(:clj (z/replace nloc new-node)
              ;;    :cljs (clojure.zip/replace nloc new-node))
              replaced-loc (z/replace nloc new-node)]
          (recur (z/right replaced-loc) (next new-loc) replaced-loc))
        (z/up last-loc)))))

(defn sort-by-ns-first
  [title]
  (fn [zloc]
    (let [main-ns (first (str/split (str title) #"\."))
          sortable (get-sortable zloc)]
      (if (str/starts-with? sortable main-ns)
        (str " " sortable)  ; add a space to force this to the top
        sortable))))

(defn sort-require
  [zloc title]
  (-> zloc
      (z/prewalk (fn [zloc] (= :refer (z/sexpr zloc)))
                 (fn [zloc] (sort-val (z/down (z/next zloc)) get-sortable)))
      z/down
      z/next
      (sort-val (sort-by-ns-first title))))

(defn sort-import
  [zloc title]
  (-> zloc
      z/down
      z/next
      (sort-val (sort-by-ns-first title))
      (z/prewalk (fn [zloc] (z/seq? zloc))
                 (fn [zloc] (sort-val (z/next (z/down zloc)) get-sortable)))))

(defn update-code
  [code]
  (if-let [zloc (-> (z/of-string code)
                    (z/find-value z/next 'ns)
                    z/up)]
    (let [title (-> zloc
                    z/down
                    z/next
                    z/sexpr)
          require-zloc (z/find-value zloc z/next ':require)
          import-zloc (z/find-value zloc z/next ':import)]
      (cond-> zloc
        require-zloc
        (z/subedit-> (z/find-value z/next ':require) z/up (sort-require title))

        import-zloc
        (z/subedit-> (z/find-value z/next ':import) z/up (sort-import title))

        true z/root-string))
    code))

(comment
  (println (update-code (slurp "/Users/alliejo/dev/cloned/adt/src/adt/outbox/impl/db.clj")))
  (def code
    "
(ns bengal.noise-test.tomjkidd
  \"Ensures all forms in bengal.noise.tomjkidd.* compile\"
  (:require bengal.noise.tomjkidd.db))
")
 (def code (slurp "test/data/broken.clj-test"))
 (println code)
 (println (update-code code)))

(defn sort-file
  "Read, update and write to file"
  [file]
  (try (let [data (slurp file)] (spit file (update-code data)))
       (catch Exception e (println (str "Cannot update file: " file) e))))


;; (defn sort-path
;;   "Filter for only .clj, .cljs, .cljc files"
;;   [path]
;;   (let [files (file-seq (io/file path))
;;         files (filter #(and (or (clojure.string/ends-with? (.getAbsolutePath %)
;;         ".clj")
;;                                 (clojure.string/ends-with? (.getAbsolutePath %)
;;                                 ".cljs")
;;                                 (clojure.string/ends-with? (.getAbsolutePath %)
;;                                 ".cljc"))
;;                             (false? (.isDirectory %)))
;;                       files)]
;;     (doseq [file files] (sort-file file))))


(defn -main
  "Sort :require block in each namespace found in src folders."
  [& files]
  (doseq [file files] (sort-file file)))
