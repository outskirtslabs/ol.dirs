(ns ^:no-doc ol.dirs.runtime.windows-ffm)

(def ^:private known-folder-ids
  {:profile "{5E6C858F-0E22-4760-9AFE-EA3317B67173}"
   :music "{4BD8D571-6D19-48D3-BE97-422220080E43}"
   :desktop "{B4BFCC3A-DB2C-424C-B029-7FE99A87C641}"
   :documents "{FDD39AD0-238F-46AF-ADB4-6C85480369C7}"
   :downloads "{374DE290-123F-4565-9164-39C4925E467B}"
   :pictures "{33E28130-4E1E-4676-835A-98395C3BC3BB}"
   :public "{DFDF76A2-C82A-4D63-906A-5644AC457385}"
   :templates "{A63293E8-664E-48DB-A079-DF759E0509F7}"
   :videos "{18989B1D-99B5-455B-841C-AB7C74E4DDFC}"
   :roaming-app-data "{3EB685DB-65F9-4CF6-A03A-E3EF65729F3D}"
   :local-app-data "{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}"
   :common-app-data "{62AB5D82-FDC1-4DC3-A9DD-070D1D495D97}"})

(defn known-folder-id [folder]
  (get known-folder-ids folder))

(defn ensure-success! [hresult folder]
  (when-not (zero? hresult)
    (throw (ex-info "Windows Known Folder lookup failed"
                    {:folder folder
                     :hresult hresult}))))

(defn known-folders []
  (throw (UnsupportedOperationException.
          "Windows Known Folder FFM lookup is not available in this environment yet.")))
