(ns quanta.tradingview.handler.history
  (:require
   [clojure.walk]
   [ring.util.response :as res]
   [quanta.tradingview.response.bars :refer [load-series]]))

(defn history-handler [{:keys [bar-db ctx query-params] :as req}]
  ; todo, return resolution other than :d and markets other than :us
  (let [db (:bar-db ctx)
        _ (println "query-params: " query-params)
        {:keys [symbol resolution from to]} (clojure.walk/keywordize-keys query-params)
        series (load-series db symbol resolution from to)]
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
