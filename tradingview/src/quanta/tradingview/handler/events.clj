(ns quanta.tradingview.handler.events
  (:require
   [ring.util.response :as res]
   [clojure.java.io :as io]
   [quanta.tradingview.events :refer [list-events load-events]]
   ))

(defn events-browse-handler [{:keys [ctx query-params] :as _req}]
  (let [events (list-events ctx)]
    (res/response events)))

(defn- id-from-request
  "Extract message entity id from request. Reitit puts path params in :path-params (string keys)."
  [request]
  (or (get (:path-params request) "id")
      (get (:path-params request) :id)))

(defn events-load-handler [{:keys [ctx] :as req}]
  (let [id (id-from-request req)
        events (load-events ctx id)]
    (res/response events)))

(defn tradingview-page-handler [_req]
  (let [html (-> "public/tvtrading.html" (io/resource) slurp)]
    (res/response html)))

(defn tradingview-events-page-handler [_req]
  (let [html (-> "public/tvevents.html" (io/resource) slurp)]
    (res/response html)))