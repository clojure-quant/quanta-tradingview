(ns quanta.tradingview.handler.history
  (:require
   [clojure.walk]
   [ring.util.response :as res]
   [quanta.tradingview.handler.response.bars :refer [load-series]]))

(defn history-handler [{:keys [ctx query-params] :as _req}]
  ; todo, return resolution other than :d and markets other than :us
  (let [bar-db (:bar-db ctx)
        _ (assert bar-db ":ctx needs bar-db")
        asset-db (:asset-db ctx)
        _ (assert asset-db ":ctx needs asset-db")
        ;countback	The number of bars to return, starting with to. 
        ;           This has higher priority than from. If countback is set, from should be ignored.
        {:keys [symbol resolution from to countback]} (clojure.walk/keywordize-keys query-params)
        series (load-series bar-db asset-db symbol resolution from to countback)]
    (res/response series)))
