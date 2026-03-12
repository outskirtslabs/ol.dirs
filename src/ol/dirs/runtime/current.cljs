(ns ^:no-doc ol.dirs.runtime.current
  (:require [ol.dirs.runtime.node :as node]))

(defn current-context []
  (node/current-context))
