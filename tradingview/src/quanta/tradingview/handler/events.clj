(ns quanta.tradingview.handler.events
  (:require
   [ring.util.response :as res]
   [clojure.java.io :as io]
   [quanta.tradingview.persist :refer [spit-edn slurp-edn]]
   ))

(defn events-handler [{:keys [ctx query-params] :as _req}]
  (let [tradingview (:tradingview ctx)
        events-path (:events-path tradingview)
        events (slurp-edn (str events-path "events.edn"))]
    (res/response events)))


(defn tradingview-page-handler [_req]
  (let [html (-> "public/tvtrading.html" (io/resource) slurp)]
    (res/response html)))

(defn tradingview-events-page-handler [_req]
  (let [html (-> "public/tvevents.html" (io/resource) slurp)]
    (res/response html)))