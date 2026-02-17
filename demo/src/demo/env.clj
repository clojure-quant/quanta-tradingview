(ns demo.env
  (:require
   [modular.system :refer [system]]))



(def env
  {:tradingview  (:tradingview (system :config))
   :default {:client-id "tradingview.com"
             :user-id "user-444"}
   ;:bar-db (system :bardb)
   :bar-db (:barsource (system :bardb-sa))
   :assetdb (system :assetdb)})