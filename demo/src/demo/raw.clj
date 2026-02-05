(ns demo.raw
  (:require
   [modular.system :refer [system]]
   [quanta.tradingview.asset.db :refer [search instrument-details]]
   [quanta.tradingview.handler.asset :refer [symbols-handler search-handler]]
   [quanta.tradingview.handler.response.asset :refer [symbol-search symbol-info]]
   [quanta.tradingview.handler.history :refer [history-handler]]))

;; use custom asset db

(def assets [{:asset "A" :name "asdf" :category "stock" :exchange "US"}
             {:asset "AA" :name "anonomynous alcoholico" :category "stock" :exchange "US"}])
(def asset-db (quanta.tradingview.asset.db/start-asset-db assets))

(instrument-details asset-db "AA")

(search asset-db "")

;; use webserver config
(def env
  {:tradingview  (:tradingview (system :config))
   :bar-db (system :duckdb)
   :asset-db (system :asset-db)})
env

(symbol-info (:asset-db env) "AA")

(instrument-details (:asset-db env) "AA")
(symbols-handler {:ctx env
                  :query-params {:symbol "AA"}})
(search-handler {:ctx env
                  :query-params {:query "A"
                                 ;:type "Equity"
                                 :limit "10"
                                 }})


(search (:asset-db env) "")


 ; history
(history-handler {:ctx env
                  :query-params {:symbol "AA"
                                 :from "1617235200"
                                 :to "1619827200"
                                 :countback "3"
                                 :resolution "D"}})

