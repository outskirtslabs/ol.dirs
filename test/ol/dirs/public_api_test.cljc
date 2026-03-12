(ns ol.dirs.public-api-test
  (:require [clojure.test :refer [deftest is testing]]
            [ol.dirs :as sut]
            [ol.dirs.runtime.current :as current]))

(def linux-context
  {:os :linux
   :home "/home/alice"
   :pid 4242
   :path-separator "/"
   :path-list-separator ":"
   :env {"XDG_DATA_HOME" "/tmp/data-home"
         "XDG_CONFIG_HOME" "/tmp/config-home"
         "XDG_STATE_HOME" "/tmp/state-home"
         "XDG_CACHE_HOME" "/tmp/cache-home"
         "XDG_RUNTIME_DIR" "/run/user/1000"
         "XDG_BIN_HOME" "/tmp/bin-home"
         "XDG_DATA_DIRS" "/opt/share:/usr/share"
         "XDG_CONFIG_DIRS" "/etc/xdg:/usr/local/etc/xdg"}
   :user-dirs-content (str "XDG_MUSIC_DIR=\"$HOME/Music\"\n"
                           "XDG_DESKTOP_DIR=\"$HOME/Desktop\"\n"
                           "XDG_DOCUMENTS_DIR=\"$HOME/Documents\"\n"
                           "XDG_DOWNLOAD_DIR=\"$HOME/Downloads\"\n"
                           "XDG_PICTURES_DIR=\"$HOME/Pictures\"\n"
                           "XDG_PUBLICSHARE_DIR=\"$HOME/Public\"\n"
                           "XDG_TEMPLATES_DIR=\"$HOME/Templates\"\n"
                           "XDG_VIDEOS_DIR=\"$HOME/Videos\"\n")})

(defn with-context [context f]
  (with-redefs [current/current-context (constantly context)]
    (f)))

(deftest storage-functions-support-zero-one-and-three-arities
  (with-context
    linux-context
    #(doseq [[f expected-base expected-one expected-three]
             [[sut/data-home "/tmp/data-home" "/tmp/data-home/my-app" "/tmp/data-home/my-app"]
              [sut/config-home "/tmp/config-home" "/tmp/config-home/my-app" "/tmp/config-home/my-app"]
              [sut/state-home "/tmp/state-home" "/tmp/state-home/my-app" "/tmp/state-home/my-app"]
              [sut/cache-home "/tmp/cache-home" "/tmp/cache-home/my-app" "/tmp/cache-home/my-app"]
              [sut/runtime-dir "/run/user/1000" "/run/user/1000/my-app" "/run/user/1000/my-app"]
              [sut/executable-dir "/tmp/bin-home" "/tmp/bin-home/my-app" "/tmp/bin-home/my-app"]
              [sut/preference-dir "/tmp/config-home" "/tmp/config-home/my-app" "/tmp/config-home/my-app"]
              [sut/state-dir "/tmp/state-home" "/tmp/state-home/my-app" "/tmp/state-home/my-app"]
              [sut/config-dir "/tmp/config-home" "/tmp/config-home/my-app" "/tmp/config-home/my-app"]]]
       (testing (str f)
         (is (= expected-base (f)))
         (is (= expected-one (f "My App")))
         (is (= expected-three (f "org" "Acme Corp" "My App")))))))

(deftest search-path-functions-append-application-path-to-each-entry
  (with-context
    linux-context
    #(do
       (is (= ["/opt/share" "/usr/share"]
              (sut/data-dirs)))
       (is (= ["/opt/share/my-app" "/usr/share/my-app"]
              (sut/data-dirs "My App")))
       (is (= ["/opt/share/my-app" "/usr/share/my-app"]
              (sut/data-dirs "org" "Acme Corp" "My App")))
       (is (= ["/etc/xdg" "/usr/local/etc/xdg"]
              (sut/config-dirs)))
       (is (= ["/etc/xdg/my-app" "/usr/local/etc/xdg/my-app"]
              (sut/config-dirs "My App")))
       (is (= ["/etc/xdg/my-app" "/usr/local/etc/xdg/my-app"]
              (sut/config-dirs "org" "Acme Corp" "My App"))))))

(deftest user-facing-directory-functions-remain-zero-arity
  (with-context
    linux-context
    #(do
       (is (= "/home/alice" (sut/home-dir)))
       (is (= "/home/alice/Music" (sut/audio-dir)))
       (is (= "/home/alice/Desktop" (sut/desktop-dir)))
       (is (= "/home/alice/Documents" (sut/document-dir)))
       (is (= "/home/alice/Downloads" (sut/download-dir)))
       (is (= "/tmp/data-home/fonts" (sut/font-dir)))
       (is (= "/home/alice/Pictures" (sut/picture-dir)))
       (is (= "/home/alice/Public" (sut/public-dir)))
       (is (= "/home/alice/Templates" (sut/template-dir)))
       (is (= "/home/alice/Videos" (sut/video-dir))))))
