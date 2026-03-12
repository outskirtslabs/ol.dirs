(ns ol.dirs.cljs-test-runner
  (:require [cljs.test :as t]
            [ol.dirs.linux-test]
            [ol.dirs.macos-test]
            [ol.dirs.project-test]
            [ol.dirs.public-api-test]
            [ol.dirs.runtime-test]
            [ol.dirs.windows-test]))

(enable-console-print!)

(defn main []
  (t/run-tests 'ol.dirs.project-test
               'ol.dirs.public-api-test
               'ol.dirs.linux-test
               'ol.dirs.macos-test
               'ol.dirs.windows-test
               'ol.dirs.runtime-test))

(set! *main-cli-fn* main)

(defmethod t/report [:cljs.test/default :end-run-tests]
  [m]
  (set! (.-exitCode js/process)
        (if (t/successful? m) 0 1)))
