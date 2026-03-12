(ns ol.dirs.runtime-test
  (:require [clojure.test :refer [deftest is]]
            [ol.dirs.impl :as sut]))

(deftest windows-home-directory-prefers-userprofile
  (is (= "C:\\Users\\Alice"
         (sut/windows-home-directory {"USERPROFILE" "C:\\Users\\Alice"
                                      "HOMEDRIVE" "D:"
                                      "HOMEPATH" "\\Ignored"}))))

(deftest windows-home-directory-falls-back-to-homedrive-and-homepath
  (is (= "C:\\Users\\Alice"
         (sut/windows-home-directory {"HOMEDRIVE" "C:"
                                      "HOMEPATH" "\\Users\\Alice"}))))

(deftest windows-home-directory-returns-nil-when-env-is-incomplete
  (is (= nil (sut/windows-home-directory {})))
  (is (= nil (sut/windows-home-directory {"HOMEDRIVE" "C:"})))
  (is (= nil (sut/windows-home-directory {"HOMEPATH" "\\Users\\Alice"}))))
