(ns ^:no-doc ol.dirs.runtime.jvm
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [ol.dirs.runtime.windows-ffm :as windows-ffm]))

(defn- os-keyword []
  (let [os-name (some-> (System/getProperty "os.name") str/lower-case)]
    (cond
      (str/includes? os-name "win") :windows
      (str/includes? os-name "mac") :macos
      :else :linux)))

(defn- read-user-dirs-content [home]
  (let [path (str home "/.config/user-dirs.dirs")
        file (io/file path)]
    (when (.isFile file)
      (slurp file))))

(defn current-context []
  (let [os (os-keyword)
        env (into {} (System/getenv))
        home (System/getProperty "user.home")
        base {:os os
              :home home
              :env env
              :pid (.pid (java.lang.ProcessHandle/current))
              :path-separator java.io.File/separator
              :path-list-separator java.io.File/pathSeparator}]
    (case os
      :linux (assoc base :user-dirs-content (read-user-dirs-content home))
      :macos base
      :windows (assoc base :folders (windows-ffm/known-folders)))))
