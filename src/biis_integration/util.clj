(ns biis-integration.util
  (:use [clojure.pprint :refer [pprint]]))

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
          (pprint (ex-data ex))
          (println "================================"))
      (println entry))))
