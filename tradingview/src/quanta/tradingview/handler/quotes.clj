(ns quanta.tradingview.handler.quotes
  (:require
   [clojure.walk]
   [ring.util.response :as res]))

(defn create-quotes [asset]
  {"s" "ok"
   "d" [{"n" asset
         "v" {"ch" 0
              "chp" 0
              "short_name" asset
              "exchange" ""
              "original_name" asset
              "description" asset
              "lp" 173.68
              "ask" 173.68
              "bid" 173.68
              "open_price" 173.68
              "high_price" 173.68
              "low_price" 173.68
              "prev_close_price" 172.77
              "volume" 173.68}}]})


(defn quote-handler [{:keys [ctx query-params] :as _req}]
  ; http://localhost:8080/tv/quotes?symbols=KO%2CDAR
  (let [_ (println "query-params: " query-params)
        ;countback	The number of bars to return, starting with to. 
        ;           This has higher priority than from. If countback is set, from should be ignored.
        {:keys [symbols]} (clojure.walk/keywordize-keys query-params)]
    (res/response (create-quotes symbols))))

