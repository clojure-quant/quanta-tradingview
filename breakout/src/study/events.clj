(ns demo.breakout.events
  (:require
   [tablecloth.api :as tc]
   [modular.persist.protocol :refer [save]]
   [modular.persist.edn] ; side effects
   [quanta.tradingview.handler.response.asset :refer [symbol-info]]
   [quanta.tradingview.chart.source :refer [->epoch]]
   [demo.env :refer [env]]))


(def events
  (-> (tc/dataset "2018-pl2.csv" {:key-fn keyword})
      (tc/select-columns [:date-instant :asset :close :trailing-high
                          :trailing-high-date :idx :pl])
      (tc/rename-columns {:date-instant :date})
      (tc/rows :as-maps)))

(def events-with-asset
  (->> events
       (filter #(:description (symbol-info (:assetdb env) (:asset %))))))

(comment 
  (symbol-info (:assetdb env) "WTR")
  (symbol-info (:assetdb env) "KO")
  (count events-with-asset)
  ;234
  )

(defn event-chart-name [event]
  (str "event-" (:asset event) "-" (:idx event)))

(defn event-text [event]
  (str (Math/round (* (:pl event) 100.0))))

; {:date #time/zoned-date-time "2022-11-03T00:00Z",
;  :asset "ABC", :close 157.84, :trailing-high 167.19,
;  :trailing-high-date #time/zoned-date-time "2022-04-21T00:00Z"})

; :asset :date :chart-name

(defn save-event-summary
  [events]
  (->> events
       (map #(update % :date ->epoch))
       (map #(assoc % :chart (event-chart-name %)))
       (map #(assoc % :text (event-text %)))
       (sort-by :pl)
       (map #(select-keys % [:asset :date :chart :text]))
       (map #(with-meta % nil)) 
       (into [])
       (save :edn "./tv/events/breakout.edn")))


(comment
  (save-event-summary events-with-asset)
 ; 
  )