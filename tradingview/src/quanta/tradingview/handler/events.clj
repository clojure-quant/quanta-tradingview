(ns quanta.tradingview.handler.events
  (:require
   [ring.util.response :as res]
   [quanta.tradingview.persist :refer [spit-edn slurp-edn]]
   ))

(defn events-handler [{:keys [ctx query-params] :as _req}]
  (let [tradingview (:tradingview ctx)
        events-path (:events-path tradingview)
        ;{:keys [symbol]} (clojure.walk/keywordize-keys query-params)
        ;si (symbol-info asset-db symbol)
        events (slurp-edn (str events-path "breakout.edn"))]
    (res/response events)))