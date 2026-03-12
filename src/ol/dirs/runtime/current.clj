(ns ^:no-doc ol.dirs.runtime.current
  (:require [ol.dirs.runtime.jvm :as jvm]))

(defn current-context []
  (jvm/current-context))
