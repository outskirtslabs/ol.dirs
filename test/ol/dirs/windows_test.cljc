(ns ol.dirs.windows-test
  (:require [clojure.test :refer [deftest is testing]]
            [ol.dirs.impl :as sut]))

(def context
  {:os :windows
   :home "C:\\Users\\Alice"
   :path-separator "\\"
   :folders {:roaming-app-data "C:\\Users\\Alice\\AppData\\Roaming"
             :local-app-data "C:\\Users\\Alice\\AppData\\Local"
             :common-app-data "C:\\ProgramData"
             :desktop "C:\\Users\\Alice\\Desktop"
             :documents "C:\\Users\\Alice\\Documents"
             :downloads "C:\\Users\\Alice\\Downloads"
             :music "C:\\Users\\Alice\\Music"
             :pictures "C:\\Users\\Alice\\Pictures"
             :public "C:\\Users\\Public"
             :templates "C:\\Users\\Alice\\AppData\\Roaming\\Microsoft\\Windows\\Templates"
             :videos "C:\\Users\\Alice\\Videos"}})

(deftest windows-resolver-uses-known-folder-context
  (let [resolved (sut/resolve-windows context)]
    (is (= "C:\\Users\\Alice\\AppData\\Roaming" (:data-home resolved)))
    (is (= "C:\\Users\\Alice\\AppData\\Roaming" (:config-home resolved)))
    (is (= "C:\\Users\\Alice\\AppData\\Local" (:state-home resolved)))
    (is (= "C:\\Users\\Alice\\AppData\\Local" (:cache-home resolved)))
    (is (= ["C:\\ProgramData"] (:data-dirs resolved)))
    (is (= ["C:\\ProgramData"] (:config-dirs resolved)))
    (is (= nil (:runtime-dir resolved)))
    (is (= nil (:executable-dir resolved)))
    (is (= "C:\\Users\\Alice\\AppData\\Roaming" (:preference-dir resolved)))
    (is (= "C:\\Users\\Alice" (:home-dir resolved)))
    (is (= "C:\\Users\\Alice\\Music" (:audio-dir resolved)))
    (is (= "C:\\Users\\Alice\\Desktop" (:desktop-dir resolved)))
    (is (= "C:\\Users\\Alice\\Documents" (:document-dir resolved)))
    (is (= "C:\\Users\\Alice\\Downloads" (:download-dir resolved)))
    (is (= nil (:font-dir resolved)))
    (is (= "C:\\Users\\Alice\\Pictures" (:picture-dir resolved)))
    (is (= "C:\\Users\\Public" (:public-dir resolved)))
    (is (= "C:\\Users\\Alice\\AppData\\Roaming\\Microsoft\\Windows\\Templates" (:template-dir resolved)))
    (is (= "C:\\Users\\Alice\\Videos" (:video-dir resolved)))))

(deftest windows-application-storage-paths-use-the-derived-project-path
    (testing "application roots are appended in the public resolver layer"
      (is (= "C:\\Users\\Alice\\AppData\\Roaming\\Acme Corp\\My App"
           (sut/resolve-directory context :config-home "org" "Acme Corp" "My App")))
      (is (= "C:\\Users\\Alice\\AppData\\Local\\Acme Corp\\My App"
           (sut/resolve-directory context :cache-home "org" "Acme Corp" "My App")))
      (is (= ["C:\\ProgramData\\Acme Corp\\My App"]
           (sut/resolve-directory context :data-dirs "org" "Acme Corp" "My App")))))
