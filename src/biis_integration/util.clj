(ns biis-integration.util
  (:require [clojure.java.io :as io]
            [java-time.api :as jt])
  (:use [clojure.pprint :refer [pprint]])
  (:import (java.io File)))

(defn process [context [f bindings]]
  (let [result (f context)
        add-binding (fn [c [key getter]]
                      (let [f-binding (if (string? getter) #(get % getter) getter)]
                        (assoc c key (f-binding result))))]
    (if bindings
      (reduce add-binding context bindings)
      context)))

(defn with-context [context & steps]
  (->> (partition 2 steps)
       (reduce process context)))

(defn log [entry & [ex]]
  (locking *out*
    (if ex
      (do (println "--------" entry "----------")
          (println (ex-message ex))
          (pprint (-> ex ex-data :response))
          (println "================================"))
      (println entry))))

(defn delete-directory-recursive [^File file]
  (when (.isDirectory file)
    (run! delete-directory-recursive (.listFiles file)))
  (io/delete-file file))

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