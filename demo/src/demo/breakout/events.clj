(ns demo.breakout.events
  (:require
   [tablecloth.api :as tc]
   [modular.persist.protocol :refer [save loadr]]
   [modular.persist.edn] ; side effects
   [quanta.tradingview.handler.response.asset :refer [symbol-info]]
   [quanta.tradingview.chart.source :refer [->epoch]]
   [demo.env :refer [env]]))


(def events
  (-> (tc/dataset "2018-pl2.csv" {:key-fn keyword})
      (tc/select-columns [:date-instant :asset :close :trailing-high
                          :trailing-high-date :idx])
      (tc/rename-columns {:date-instant :date})
      (tc/rows :as-maps)))

(def events-with-asset
  (->> events
       (filter #(:description (symbol-info (:asset-db env) (:asset %))))))


(defn event-chart-name [event]
  (str "event-" (:asset event) "-" (:idx event)))


; {:date #time/zoned-date-time "2022-11-03T00:00Z",
;  :asset "ABC", :close 157.84, :trailing-high 167.19,
;  :trailing-high-date #time/zoned-date-time "2022-04-21T00:00Z"})

; :asset :date :chart-name

(defn save-event-summary
  [events]
  (->> events
       (map #(update % :date ->epoch))
       (map #(assoc % :chart (event-chart-name %)))
       (map #(select-keys % [:asset :date :chart]))
       (map #(with-meta % nil)) 
       (into [])
       (save :edn "./tv/events/breakout.edn")))


(comment
  (save-event-summary events-with-asset)
 ; 
  )