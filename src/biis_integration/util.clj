(ns biis-integration.util
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [java-time.api :as jt])
  (:import (java.io File)))

(defn process [context [f bindings]]
  (let [result (f context)
        add-binding (fn [c [key getter]]
                      (let [f-binding (cond (string? getter) #(get % getter)
                                            (vector? getter) #(get-in % getter)
                                            :else getter)]
                        (assoc c key (f-binding result))))]
    (if bindings
      (reduce add-binding context bindings)
      context)))

(defn enrich [context & steps]
  (->> (partition 2 steps)
       (reduce process context)))

(defn enrich-when [context test & steps]
  (if (test context)
    (apply enrich context steps)
    context))

(defn log [entry & [ex]]
  (locking *out*
    (if ex
      (do (println "--------" entry "----------")
          (println (ex-message ex))
          (pprint (-> ex ex-data :response))
          (println "================================"))
      (if (map? entry) (pprint entry)
                       (println entry)))))

(defn create-folder [^String path]
  (let [folder (File. path)]
    (when-not (.exists folder) (.mkdir folder))))

(defn delete-directory-recursive [^File file]
  (when (.isDirectory file)
    (run! delete-directory-recursive (.listFiles file)))
  (io/delete-file file))

(defn truncate-folder [^String path]
  (let [folder (File. path)]
    (when (.exists folder) (delete-directory-recursive folder))
    (.mkdir folder)))

(defn deep-merge [v & vs]
  (letfn [(rec-merge [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (when (some identity vs)
      (reduce #(rec-merge %1 %2) v vs))))

(defn format-date [d]
  (->> (jt/truncate-to d :days) (jt/format "yyyy-MM-dd'T'HH:mm:ss")))

(def today (-> (jt/local-date-time) format-date))

(def tomorrow (-> (jt/local-date-time)
                  (jt/plus (jt/days 1))
                  format-date))
