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
        ;           This has higher priority than from. If countback is set, from should be ignored.
        {:keys [symbol resolution from to countback]} (clojure.walk/keywordize-keys query-params)
        series (load-series db symbol resolution from to countback)]
    (res/response series)))
