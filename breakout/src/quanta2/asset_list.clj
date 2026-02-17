(ns quanta2.asset-list
  (:require
   [datahike.api :as d]
   [quanta.market.asset.datahike :refer [get-list]]))

(defn only-names [lists]
  (map :lists/name lists))

(defn get-lists [dbconn]
  (-> '[:find [(pull ?id [:lists/name]) ...]
        :in $ ; ?asset-symbol
        :where
        [?id :lists/name _]]
      (d/q @dbconn)
      (only-names)))

(defn show-lists [{:keys [assetdb]}]
  (get-lists assetdb))

(defn assets [env {:keys [asset list]}]
  (->>  (concat
         (if list (:lists/asset (get-list (:assetdb env) list)) [])
         (cond
           (string? asset) [asset]
           (or (seq? asset) (vector? asset)) asset
           :else []))
        (into #{})))

(comment
  (require '[demo.env :refer [env]])
  (get-lists (:assetdb env))
  (get-list (:assetdb env) "")
  (get-list (:assetdb env) "flo")
  (assets env {:asset "AEE.AX"})
  (assets env {:asset ["AEE.AX" "CWI.VI"]})
  (assets env {:list "flo"})
  (assets env {:list "flo" :asset "AEE.AX"})
  (assets env {:list "flo" :asset ["AEE.AX" "CWI.VI"]})

  (map println (assets env {:list "flo"}))
 ; 
  )
