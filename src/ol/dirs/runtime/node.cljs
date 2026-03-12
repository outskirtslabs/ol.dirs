(ns ^:no-doc ol.dirs.runtime.node
  (:require ["fs" :as fs]
            ["os" :as os]
            ["path" :as node-path]
            ["process" :as process]
            [ol.dirs.impl :as impl]))

(defn- platform-keyword []
  (case process/platform
    "win32" :windows
    "darwin" :macos
    :linux))

(defn- read-user-dirs-content [home]
  (let [user-dirs-path (.join node-path home ".config" "user-dirs.dirs")]
    (when (.existsSync fs user-dirs-path)
      (.readFileSync fs user-dirs-path "utf8"))))

(defn- windows-folders [env home]
  {:roaming-app-data (get env "APPDATA")
   :local-app-data (get env "LOCALAPPDATA")
   :common-app-data (get env "PROGRAMDATA")
   :desktop (.join node-path home "Desktop")
   :documents (.join node-path home "Documents")
   :downloads (.join node-path home "Downloads")
   :music (.join node-path home "Music")
   :pictures (.join node-path home "Pictures")
   :public (or (get env "PUBLIC") (.join node-path (or (get env "SystemDrive") "C:\\") "Users" "Public"))
   :templates (when-let [appdata (get env "APPDATA")]
                (.join node-path appdata "Microsoft" "Windows" "Templates"))
   :videos (.join node-path home "Videos")})

(defn current-context []
  (let [env (js->clj process/env)
        os-key (platform-keyword)
        home (or (when (= os-key :windows)
                   (impl/windows-home-directory env))
                 (.homedir os))
        base {:os os-key
              :home home
              :env env
              :pid process/pid
              :path-separator (.-sep node-path)
              :path-list-separator (.-delimiter node-path)}]
    (case os-key
      :linux (assoc base :user-dirs-content (read-user-dirs-content home))
      :macos base
      :windows (assoc base :folders (windows-folders env home)))))
