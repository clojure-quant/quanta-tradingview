(ns demo.handler-demo
  (:require
   [quanta.tradingview.handler.asset :refer [symbols-handler search-handler]]
   [quanta.tradingview.handler.response.asset :refer [symbol-search symbol-info]]
   [quanta.tradingview.handler.history :refer [history-handler]]
   [quanta.tradingview.handler.config :refer [calc-exchanges calc-categories]]
   [demo.env :refer [env]]))


env

;; DETAILS ONE ASSET
(symbol-info (:assetdb env) "AA")

(symbols-handler {:ctx env
                  :query-params {:symbol "AA"}})

;; SEARCH 

(symbol-search (:assetdb env) "A" "etf" "" 2)

(search-handler {:ctx env
                 :query-params {:query "A"
                                :type "etf"
                                :limit "10"}})
;; CONFIG

(calc-exchanges env)

(calc-categories env)

 ; history
(history-handler {:ctx env
                  :query-params {:symbol "AA"
                                 :from "1617235200"
                                 :to "1619827200"
                                 :countback "3"
                                 :resolution "D"}})

