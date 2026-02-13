(ns quanta.tradingview.handler.asset
  (:require
   [clojure.walk]
   [ring.util.response :as res]
   [quanta.tradingview.handler.response.asset :refer [symbol-search symbol-info]]))

;https://demo_feed.tradingview.com/search?query=B&type=stock&exchange=NYSE&limit=10
;[{"symbol":"BLK","full_name":"BLK","description":"BlackRock, Inc.","exchange":"NYSE","type":"stock"},
;  {"symbol":"BA","full_name":"BA","description":"The Boeing Company","exchange":"NYSE","type":"stock"}]

(defn search-handler [{:keys [ctx query-params] :as _req}]
  (let [assetdb (:assetdb ctx)
        {:keys [query type exchange limit]} (clojure.walk/keywordize-keys query-params)
        limit (Integer/parseInt limit)
        result-tv (symbol-search assetdb query type exchange limit)]
    (res/response result-tv)))

;https://demo_feed.tradingview.com/symbols?symbol=FX%3AEURUSD

(defn symbols-handler [{:keys [ctx query-params] :as _req}]
  (let [assetdb (:assetdb ctx)
        {:keys [symbol]} (clojure.walk/keywordize-keys query-params)
        si (symbol-info assetdb symbol)]
    (res/response si)))