(ns ol.dirs.project-test
  (:require [clojure.test :refer [deftest is testing]]
            [ol.dirs.impl :as sut]))

(deftest linux-application-path-uses-normalized-application-name
  (testing "linux only keeps the application segment and normalizes whitespace"
    (is (= "foo-bar-app"
           (sut/application-path :linux "org.example" "Baz Corp" " Foo  Bar App ")))
    (is (= "example-program"
           (sut/application-path :linux nil nil "Example Program")))))

(deftest macos-application-path-uses-bundle-style-format
  (testing "macOS includes qualifier, organization, and application segments"
    (is (= "org.Baz-Corp.Foo-Bar-App"
           (sut/application-path :macos "org" "Baz Corp" "Foo Bar App")))
    (is (= "Baz-Corp.Foo-Bar-App"
           (sut/application-path :macos nil "Baz Corp" "Foo Bar App")))
    (is (= "org.Foo-Bar-App"
           (sut/application-path :macos "org" nil "Foo Bar App")))))

(deftest windows-application-path-uses-organization-and-application
  (testing "windows ignores the qualifier and preserves the visible names"
    (is (= "Baz Corp\\Foo Bar App"
           (sut/application-path :windows "org" "Baz Corp" "Foo Bar App")))
    (is (= "Foo Bar App"
           (sut/application-path :windows nil nil "Foo Bar App")))))
