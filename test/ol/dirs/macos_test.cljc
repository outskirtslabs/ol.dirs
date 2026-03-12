(ns ol.dirs.macos-test
  (:require [clojure.test :refer [deftest is]]
            [ol.dirs.impl :as sut]))

(def context
  {:os :macos
   :home "/Users/alice"
   :path-separator "/"})

(deftest macos-resolver-uses-standard-library-and-home-directories
  (let [resolved (sut/resolve-macos context)]
    (is (= "/Users/alice/Library/Application Support" (:data-home resolved)))
    (is (= "/Users/alice/Library/Application Support" (:config-home resolved)))
    (is (= "/Users/alice/Library/Application Support" (:state-home resolved)))
    (is (= "/Users/alice/Library/Caches" (:cache-home resolved)))
    (is (= ["/Library/Application Support"] (:data-dirs resolved)))
    (is (= ["/Library/Application Support"] (:config-dirs resolved)))
    (is (= nil (:runtime-dir resolved)))
    (is (= nil (:executable-dir resolved)))
    (is (= "/Users/alice/Library/Preferences" (:preference-dir resolved)))
    (is (= "/Users/alice" (:home-dir resolved)))
    (is (= "/Users/alice/Music" (:audio-dir resolved)))
    (is (= "/Users/alice/Desktop" (:desktop-dir resolved)))
    (is (= "/Users/alice/Documents" (:document-dir resolved)))
    (is (= "/Users/alice/Downloads" (:download-dir resolved)))
    (is (= "/Users/alice/Library/Fonts" (:font-dir resolved)))
    (is (= "/Users/alice/Pictures" (:picture-dir resolved)))
    (is (= "/Users/alice/Public" (:public-dir resolved)))
    (is (= nil (:template-dir resolved)))
    (is (= "/Users/alice/Movies" (:video-dir resolved)))))
