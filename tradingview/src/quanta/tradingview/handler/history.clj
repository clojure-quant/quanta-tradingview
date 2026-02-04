(ns quanta.tradingview.handler.history
  (:require
   [clojure.walk]
   [ring.util.response :as res]
   [quanta.tradingview.handler.response.bars :refer [load-series]]))

(defn history-handler [{:keys [ctx query-params] :as _req}]
  ; todo, return resolution other than :d and markets other than :us
  (let [db (:bar-db ctx)
        _ (println "query-params: " query-params)
        ;countback	The number of bars to return, starting with to. 
        ;           This has higher priority than from. If countback is set, 
        ;           from should be ignored.
        {:keys [symbol resolution from to countback]} (clojure.walk/keywordize-keys query-params)
        series (load-series db symbol resolution from to countback)]
    (res/response series)))

(comment

  ; test load known symbol
  (history-handler {:query-params {:symbol "ETHUSD"
                                   :from "1617235200"
                                   :to "1619827200"
                                   :resolution "D"}})

  ; test for unknown symbol
  (history-handler {:query-params {:symbol "XXXX"
                                   :from "1617235200"
                                   :to "1619827200"
                                   :resolution "D"}})

  (history-handler {:query-params {:symbol "Unknown-"
                                   :from "1617235200"
                                   :to "1619827200"
                                   :resolution "D"}})

  (history-handler {:query-params   {"symbol" "BTCUSD"
                                     "resolution" "D"
                                     "from" "1299075015"
                                     "to" "1303308614"}})
;
  )
