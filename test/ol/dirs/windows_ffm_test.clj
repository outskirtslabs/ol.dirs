(ns ol.dirs.windows-ffm-test
  (:require [clojure.test :refer [deftest is testing]]
            [ol.dirs.runtime.windows-ffm :as sut]))

(deftest known-folder-ids-cover-the-public-api-surface
  (is (= "{5E6C858F-0E22-4760-9AFE-EA3317B67173}"
         (sut/known-folder-id :profile)))
  (is (= "{3EB685DB-65F9-4CF6-A03A-E3EF65729F3D}"
         (sut/known-folder-id :roaming-app-data)))
  (is (= "{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}"
         (sut/known-folder-id :local-app-data))))

(deftest ensure-success-throws-for-failing-hresults
  (is (= nil (sut/ensure-success! 0 :profile)))
  (testing "non-zero HRESULTs bubble up as ex-info with folder context"
    (let [ex (try
               (sut/ensure-success! 5 :profile)
               nil
               (catch clojure.lang.ExceptionInfo ex
                 ex))]
      (is ex)
      (is (= {:folder :profile :hresult 5}
             (ex-data ex))))))
