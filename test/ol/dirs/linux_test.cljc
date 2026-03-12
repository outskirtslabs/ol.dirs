(ns ol.dirs.linux-test
  (:require [clojure.test :refer [deftest is testing]]
            [ol.dirs.impl :as sut]))

(def base-context
  {:os :linux
   :home "/home/alice"
   :pid 4242
   :path-separator "/"
   :path-list-separator ":"
   :env {}
   :user-dirs-content nil})

(deftest xdg-defaults-are-used-when-environment-is-missing
  (let [resolved (sut/resolve-linux base-context)]
    (is (= "/home/alice/.local/share" (:data-home resolved)))
    (is (= "/home/alice/.config" (:config-home resolved)))
    (is (= "/home/alice/.local/state" (:state-home resolved)))
    (is (= "/home/alice/.cache" (:cache-home resolved)))
    (is (= ["/usr/local/share" "/usr/share"] (:data-dirs resolved)))
    (is (= ["/etc/xdg"] (:config-dirs resolved)))
    (is (= nil (:runtime-dir resolved)))
    (is (= "/home/alice/.local/bin" (:executable-dir resolved)))
    (is (= "/home/alice/.config" (:preference-dir resolved)))
    (is (= "/home/alice/.local/share/fonts" (:font-dir resolved)))))

(deftest relative-xdg-values-are-ignored
  (let [resolved (sut/resolve-linux (assoc base-context
                                           :env {"XDG_DATA_HOME" "relative/data"
                                                 "XDG_CONFIG_HOME" "config"
                                                 "XDG_STATE_HOME" "state"
                                                 "XDG_CACHE_HOME" "cache"
                                                 "XDG_RUNTIME_DIR" "run/user/1000"
                                                 "XDG_BIN_HOME" "bin"
                                                 "XDG_DATA_DIRS" "relative:/opt/share"
                                                 "XDG_CONFIG_DIRS" "relative:/etc/xdg"}))]
    (is (= "/home/alice/.local/share" (:data-home resolved)))
    (is (= "/home/alice/.config" (:config-home resolved)))
    (is (= "/home/alice/.local/state" (:state-home resolved)))
    (is (= "/home/alice/.cache" (:cache-home resolved)))
    (is (= nil (:runtime-dir resolved)))
    (is (= "/home/alice/.local/bin" (:executable-dir resolved)))
    (is (= ["/usr/local/share" "/usr/share"] (:data-dirs resolved)))
    (is (= ["/etc/xdg"] (:config-dirs resolved)))))

(deftest trusted-systemd-directories-override-config-state-and-cache
  (let [trusted (sut/resolve-linux (assoc base-context
                                          :env {"SYSTEMD_EXEC_PID" "4242"
                                                "CONFIGURATION_DIRECTORY" "/etc/myapp:/etc/other"
                                                "STATE_DIRECTORY" "/var/lib/myapp:/var/lib/other"
                                                "CACHE_DIRECTORY" "/var/cache/myapp:/var/cache/other"}))
        invocation-trusted (sut/resolve-linux (assoc base-context
                                                     :env {"INVOCATION_ID" "abc123"
                                                           "CONFIGURATION_DIRECTORY" "/etc/unit"
                                                           "STATE_DIRECTORY" "/var/lib/unit"
                                                           "CACHE_DIRECTORY" "/var/cache/unit"}))
        untrusted (sut/resolve-linux (assoc base-context
                                            :env {"SYSTEMD_EXEC_PID" "9999"
                                                  "CONFIGURATION_DIRECTORY" "/etc/bad"
                                                  "STATE_DIRECTORY" "/var/lib/bad"
                                                  "CACHE_DIRECTORY" "/var/cache/bad"}))]
    (testing "matching SYSTEMD_EXEC_PID trusts systemd values and selects the first path"
      (is (= "/etc/myapp" (:config-home trusted)))
      (is (= "/var/lib/myapp" (:state-home trusted)))
      (is (= "/var/cache/myapp" (:cache-home trusted))))
    (testing "INVOCATION_ID also trusts systemd values"
      (is (= "/etc/unit" (:config-home invocation-trusted)))
      (is (= "/var/lib/unit" (:state-home invocation-trusted)))
      (is (= "/var/cache/unit" (:cache-home invocation-trusted))))
    (testing "mismatched pid without INVOCATION_ID ignores systemd directory variables"
      (is (= "/home/alice/.config" (:config-home untrusted)))
      (is (= "/home/alice/.local/state" (:state-home untrusted)))
      (is (= "/home/alice/.cache" (:cache-home untrusted))))))

(deftest user-dirs-content-is-parsed-and-unresolved-values-return-nil
  (let [resolved (sut/resolve-linux (assoc base-context
                                           :user-dirs-content
                                           (str "# comment\n"
                                                "XDG_MUSIC_DIR=\"$HOME/Music\"\n"
                                                "XDG_DESKTOP_DIR=\"$HOME/\"\n"
                                                "XDG_DOCUMENTS_DIR=\"relative/Documents\"\n"
                                                "XDG_DOWNLOAD_DIR=\"$HOME/Downloads\"\n"
                                                "XDG_PICTURES_DIR=\"$HOME/Pictures\"\n"
                                                "XDG_PUBLICSHARE_DIR=\"$HOME/Public\"\n"
                                                "XDG_TEMPLATES_DIR=\"$HOME/Templates\"\n"
                                                "XDG_VIDEOS_DIR=\"$HOME/Videos\"\n")))]
    (is (= "/home/alice/Music" (:audio-dir resolved)))
    (is (= nil (:desktop-dir resolved)))
    (is (= nil (:document-dir resolved)))
    (is (= "/home/alice/Downloads" (:download-dir resolved)))
    (is (= "/home/alice/Pictures" (:picture-dir resolved)))
    (is (= "/home/alice/Public" (:public-dir resolved)))
    (is (= "/home/alice/Templates" (:template-dir resolved)))
    (is (= "/home/alice/Videos" (:video-dir resolved)))))

(deftest missing-user-dirs-content-leaves-linux-user-folders-unresolved
  (let [resolved (sut/resolve-linux base-context)]
    (is (= nil (:audio-dir resolved)))
    (is (= nil (:desktop-dir resolved)))
    (is (= nil (:document-dir resolved)))
    (is (= nil (:download-dir resolved)))
    (is (= nil (:picture-dir resolved)))
    (is (= nil (:public-dir resolved)))
    (is (= nil (:template-dir resolved)))
    (is (= nil (:video-dir resolved)))))
