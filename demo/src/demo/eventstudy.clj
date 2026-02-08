(ns demo.eventstudy
  (:require
   [clojure.pprint :refer [print-table]]
   [com.rpl.specter :as specter]
   [tablecloth.api :as tc]
   [quanta.tradingview.handler.response.asset :refer [symbol-search symbol-info]]
   [quanta.tradingview.chart.db :refer [save-chart load-chart]]
   [quanta.tradingview.chart.edit :refer [describe-charts
                                          keep-only-main-chart
                                          remove-drawings
                                          add-drawing
                                          modify-chart set-chart-asset
                                          set-axes-sources]]
   [demo.env :refer [env]]))


(def events
  (-> (tc/dataset "2018-pl2.csv" {:key-fn keyword})
      (tc/select-columns [:date-instant :asset :close :trailing-high
                          :trailing-high-date :idx])
      (tc/rename-columns {:date-instant :date})
      (tc/rows :as-maps)))

events

(count events)
; 301

(symbol-info (:asset-db env) "WTR")

(symbol-info (:asset-db env) "KO")

(def events-with-asset 
  (->> events
       (filter #(:description (symbol-info (:asset-db env) (:asset %))))))

(count events-with-asset)
;234





(defn create-chart-for-event [{:keys [date asset close
                                      trailing-high trailing-high-date]}]

  (->> (load-chart env {:chart-id "1770419848"})
       (modify-chart {:chart-id "eventtest"
                      :name "eventtest"
                      :layout "s" ; s is the single asset layout
                      :symbol asset
                      :short_name asset
                      :legs [{:symbol asset, :pro_symbol (str "us:" asset)}]})
       (set-chart-asset {:asset asset})
       (keep-only-main-chart)
       (remove-drawings)
       (add-drawing {:type "LineToolText"
                     :asset asset
                     :interval "1D"
                     :state {:text "breakout!!"}
                     :points [{:time_t date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (add-drawing {:type "LineToolVertLine"
                     :asset asset
                     :interval "1D"
                     :state {:text "breakout-date"}
                     :points [{:time_t date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (add-drawing {:type "LineToolVertLine"
                     :asset asset
                     :interval "1D"
                     :state {:text "trailing-window-high-date"}
                     :points [{:time_t trailing-high-date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (add-drawing {:type "LineToolHorzRay"
                     :asset asset
                     :interval "1D"
                     :state {:text "trailing-window-high-price"}
                     :points [{:time_t trailing-high-date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (set-axes-sources)))


(-> (load-chart env {:chart-id "eventtest"})
    (describe-charts)
    (print-table))

(-> (load-chart env {:chart-id "eventtest"})
    (describe-charts)
    (print-table))



(create-chart-for-event
 {:date #time/zoned-date-time "2022-11-03T00:00Z",
  :asset "ABC", :close 157.84, :trailing-high 167.19,
  :trailing-high-date #time/zoned-date-time "2022-04-21T00:00Z"})

(->> (create-chart-for-event (get events 1))
     (modify-chart {:chart-id (str "event-demo")
                    :name "event-demo"})
     (save-chart env {}))

(defn create-chart-asset-event [event]
  (let [chart-name (str "event-" (:asset event) "-" (:idx event))]
    (->> (create-chart-for-event event)
         (modify-chart {:chart-id chart-name
                        :name chart-name})
         (save-chart env {}))))

(doall (map create-chart-asset-event events-with-asset))
