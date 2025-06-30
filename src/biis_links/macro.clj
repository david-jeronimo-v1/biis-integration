(ns biis-links.macro
  (:import (java.util.concurrent TimeUnit)))

(defmacro with-timeout [ms & body]
  `(let [f# (future ~@body)]
     (.get f# ~ms TimeUnit/MILLISECONDS)))
