(ns biis-links.check-external-links
  (:require [clojure.string :refer [split-lines split]]
            [biis-links.macro :refer [with-timeout]]))

(defn url-error [url]
  (try
    (with-timeout 5000
                  (slurp url)
                  url)
    (catch Exception e [url (str e)])))

(defn check-links [external-links-file-path]
  (let [add-https (fn [s] (if (and s (.startsWith s "//")) (str "https:" s) s))
        urls (->> (slurp external-links-file-path)
                  (split-lines)
                  (map (comp add-https second #(split % #"'")))
                  (filter some?))
        {valid true invalid false} (->> (pmap url-error urls)
                                        (group-by string?)
                                        (#(update-vals % sort)))]
    (shutdown-agents)
    (println "Invalid")
    (run! println invalid)
    (println "Valid")
    (run! println valid)))


