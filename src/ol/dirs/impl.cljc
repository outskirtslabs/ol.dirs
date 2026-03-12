(ns ol.dirs.impl
  (:require [clojure.string :as str]))

(defn windows-home-directory [env]
  (or (get env "USERPROFILE")
      (let [drive (get env "HOMEDRIVE")
            path (get env "HOMEPATH")]
        (when (and drive path)
          (str drive path)))))

(defn- blank->nil [value]
  (when-not (str/blank? value)
    value))

(defn- trim-leading-separators [value separator]
  (loop [s value]
    (if (and (seq s) (str/starts-with? s separator))
      (recur (subs s (count separator)))
      s)))

(defn- trim-trailing-separators [value separator]
  (loop [s value]
    (if (and (> (count s) (count separator))
             (str/ends-with? s separator))
      (recur (subs s 0 (- (count s) (count separator))))
      s)))

(defn join
  ([separator left right]
   (cond
     (nil? left) right
     (nil? right) left
     (= left separator) (str left (trim-leading-separators right separator))
     :else (str (trim-trailing-separators left separator)
                separator
                (trim-leading-separators right separator))))
  ([separator left middle right]
   (join separator (join separator left middle) right)))

(defn absolute-path? [os value]
  (let [value (blank->nil value)]
    (boolean
     (case os
       :windows (or (re-find #"(?i)^[a-z]:[\\/]" value)
                    (str/starts-with? value "\\\\"))
       (str/starts-with? value "/")))))

(defn split-absolute-path-list [os separator value]
  (let [value (blank->nil value)]
    (when value
      (let [pattern (case separator
                      ":" #":"
                      ";" #";"
                      (re-pattern separator))
            parts (->> (str/split value pattern)
                       (remove str/blank?)
                       vec)]
        (when (and (seq parts) (every? #(absolute-path? os %) parts))
          parts)))))

(defn map-join [separator roots fragment]
  (mapv #(join separator % fragment) roots))

(defn normalize-unix-path [value]
  (let [absolute? (str/starts-with? value "/")
        parts (str/split value #"/+")]
    (->> parts
         (reduce (fn [acc part]
                   (cond
                     (or (= "" part) (= "." part)) acc
                     (= ".." part) (if (seq acc) (pop acc) acc)
                     :else (conj acc part)))
                 [])
         (str/join "/")
         (#(str (when absolute? "/") %))
         blank->nil)))

(defn- trim-collapse-whitespace [value separator lowercase?]
  (some-> value
          str/trim
          (str/replace #"\s+" separator)
          (#(if lowercase? (str/lower-case %) %))
          not-empty))

(defn application-path [os qualifier organization application]
  (case os
    :linux (trim-collapse-whitespace application "-" true)
    :macos (->> [qualifier organization application]
                (keep #(trim-collapse-whitespace % "-" false))
                (str/join ".")
                not-empty)
    :windows (->> [organization application]
                  (keep #(some-> % str/trim not-empty))
                  (str/join "\\")
                  not-empty)))

(defn- trusted-systemd-environment? [{:keys [env pid]}]
  (let [exec-pid (get env "SYSTEMD_EXEC_PID")
        invocation-id (get env "INVOCATION_ID")]
    (or (= (some-> exec-pid str/trim) (some-> pid str))
        (not (str/blank? invocation-id)))))

(defn- first-systemd-override [ctx env-key]
  (when (trusted-systemd-environment? ctx)
    (some->> (get-in ctx [:env env-key])
             (#(str/split % #":"))
             (remove str/blank?)
             (filter #(absolute-path? (:os ctx) %))
             first)))

(def ^:private xdg-user-dir-map
  {"XDG_MUSIC_DIR" :audio-dir
   "XDG_DESKTOP_DIR" :desktop-dir
   "XDG_DOCUMENTS_DIR" :document-dir
   "XDG_DOWNLOAD_DIR" :download-dir
   "XDG_PICTURES_DIR" :picture-dir
   "XDG_PUBLICSHARE_DIR" :public-dir
   "XDG_TEMPLATES_DIR" :template-dir
   "XDG_VIDEOS_DIR" :video-dir})

(defn- parse-user-dirs-content [content]
  (when content
    (reduce (fn [acc line]
              (let [line (str/trim line)]
                (if-let [[_ env-key value] (re-matches #"([A-Z_]+)=\"([^\"]*)\"" line)]
                  (assoc acc env-key value)
                  acc)))
            {}
            (str/split-lines content))))

(defn- resolve-user-dir-entry [home raw-value]
  (let [resolved (some-> raw-value
                         (str/replace "$HOME" home)
                         str/trim
                         not-empty)]
    (when (and resolved
               (absolute-path? :linux resolved)
               (not= resolved home)
               (not= resolved (str home "/")))
      resolved)))

(defn- resolve-user-dirs [home content]
  (let [entries (parse-user-dirs-content content)]
    (reduce-kv (fn [acc env-key dir-key]
                 (assoc acc dir-key (resolve-user-dir-entry home (get entries env-key))))
               {}
               xdg-user-dir-map)))

(defn resolve-linux [{:keys [home user-dirs-content] :as ctx}]
  (let [env-path (fn [key fallback]
                   (or (some->> (get-in ctx [:env key])
                                blank->nil
                                (#(when (absolute-path? :linux %) %)))
                       fallback))
        data-home (env-path "XDG_DATA_HOME" (join "/" home ".local/share"))
        config-home (or (first-systemd-override ctx "CONFIGURATION_DIRECTORY")
                        (env-path "XDG_CONFIG_HOME" (join "/" home ".config")))
        state-home (or (first-systemd-override ctx "STATE_DIRECTORY")
                       (env-path "XDG_STATE_HOME" (join "/" home ".local/state")))
        cache-home (or (first-systemd-override ctx "CACHE_DIRECTORY")
                       (env-path "XDG_CACHE_HOME" (join "/" home ".cache")))
        runtime-dir (some->> (get-in ctx [:env "XDG_RUNTIME_DIR"])
                             blank->nil
                             (#(when (absolute-path? :linux %) %)))
        executable-dir (or (some->> (get-in ctx [:env "XDG_BIN_HOME"])
                                    blank->nil
                                    (#(when (absolute-path? :linux %) %)))
                           (normalize-unix-path (join "/" data-home ".." "bin")))
        data-dirs (or (split-absolute-path-list :linux ":" (get-in ctx [:env "XDG_DATA_DIRS"]))
                      ["/usr/local/share" "/usr/share"])
        config-dirs (or (split-absolute-path-list :linux ":" (get-in ctx [:env "XDG_CONFIG_DIRS"]))
                        ["/etc/xdg"])]
    (merge {:data-home data-home
            :config-home config-home
            :state-home state-home
            :cache-home cache-home
            :data-dirs data-dirs
            :config-dirs config-dirs
            :runtime-dir runtime-dir
            :executable-dir executable-dir
            :preference-dir config-home
            :home-dir home
            :font-dir (join "/" data-home "fonts")}
           (resolve-user-dirs home user-dirs-content))))

(defn resolve-macos [{:keys [home]}]
  {:data-home (join "/" home "Library/Application Support")
   :config-home (join "/" home "Library/Application Support")
   :state-home (join "/" home "Library/Application Support")
   :cache-home (join "/" home "Library/Caches")
   :data-dirs ["/Library/Application Support"]
   :config-dirs ["/Library/Application Support"]
   :runtime-dir nil
   :executable-dir nil
   :preference-dir (join "/" home "Library/Preferences")
   :home-dir home
   :audio-dir (join "/" home "Music")
   :desktop-dir (join "/" home "Desktop")
   :document-dir (join "/" home "Documents")
   :download-dir (join "/" home "Downloads")
   :font-dir (join "/" home "Library/Fonts")
   :picture-dir (join "/" home "Pictures")
   :public-dir (join "/" home "Public")
   :template-dir nil
   :video-dir (join "/" home "Movies")})

(defn resolve-windows [{:keys [home folders]}]
  {:data-home (:roaming-app-data folders)
   :config-home (:roaming-app-data folders)
   :state-home (:local-app-data folders)
   :cache-home (:local-app-data folders)
   :data-dirs [(:common-app-data folders)]
   :config-dirs [(:common-app-data folders)]
   :runtime-dir nil
   :executable-dir nil
   :preference-dir (:roaming-app-data folders)
   :home-dir home
   :audio-dir (:music folders)
   :desktop-dir (:desktop folders)
   :document-dir (:documents folders)
   :download-dir (:downloads folders)
   :font-dir nil
   :picture-dir (:pictures folders)
   :public-dir (:public folders)
   :template-dir (:templates folders)
   :video-dir (:videos folders)})

(def ^:private app-aware-keys
  #{:data-home :config-home :state-home :data-dirs :config-dirs
    :cache-home :runtime-dir :executable-dir :preference-dir
    :state-dir :config-dir})

(defn base-directories [context]
  (case (:os context)
    :linux (resolve-linux context)
    :macos (resolve-macos context)
    :windows (resolve-windows context)))

(defn resolve-directory [context key & args]
  (let [resolved (base-directories context)
        key (case key
              :state-dir :state-home
              :config-dir :config-home
              key)
        base (get resolved key)
        fragment (when (app-aware-keys key)
                   (case (count args)
                     0 nil
                     1 (application-path (:os context) nil nil (first args))
                     3 (apply application-path (:os context) args)
                     (throw (ex-info "Wrong number of args" {:arg-count (count args)}))))
        separator (:path-separator context)]
    (cond
      (nil? fragment) base
      (vector? base) (map-join separator base fragment)
      (string? base) (join separator base fragment)
      :else base)))
