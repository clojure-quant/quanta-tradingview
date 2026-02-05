(ns quanta.tradingview.handler.asset
  (:require
   [clojure.walk]
   [ring.util.response :as res]
   [quanta.tradingview.handler.response.asset :refer [symbol-search symbol-info]]))

;https://demo_feed.tradingview.com/search?query=B&type=stock&exchange=NYSE&limit=10
;[{"symbol":"BLK","full_name":"BLK","description":"BlackRock, Inc.","exchange":"NYSE","type":"stock"},
;  {"symbol":"BA","full_name":"BA","description":"The Boeing Company","exchange":"NYSE","type":"stock"}]

(defn search-handler [{:keys [ctx query-params] :as _req}]
  (let [asset-db (:asset-db ctx)
        {:keys [query type exchange limit]} (clojure.walk/keywordize-keys query-params)
        limit (Integer/parseInt limit)
        result-tv (symbol-search asset-db query type exchange limit)]
    (res/response result-tv)))

(defn symbols-handler [{:keys [ctx query-params] :as _req}]
  (let [asset-db (:asset-db ctx)
        _ (println "symbols handler asset-db " asset-db)
        {:keys [symbol]} (clojure.walk/keywordize-keys query-params)
        si (symbol-info asset-db symbol)]
    (res/response si)))