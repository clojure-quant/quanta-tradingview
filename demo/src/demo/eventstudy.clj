(ns demo.eventstudy
  (:require
   [clojure.pprint :refer [print-table]]
   [com.rpl.specter :as specter]
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.tradingview.chart.db :refer [save-chart chart-list load-chart]]
   [quanta.tradingview.chart.edit :refer [describe-charts
                                          pane-owner  keep-only-main-chart
                                          remove-drawings
                                          add-drawing
                                          modify-chart set-chart-asset
                                          set-axes-sources
                                          ]]
   [demo.env :refer [env]]))




(-> (load-chart env {:chart-id "1770419848"})
    (describe-charts)
    (print-table))

; | :chart | :pane | :source |             :type |       :id | :asset |
; |--------+-------+---------+-------------------+-----------+--------|
; |      0 |     0 |       0 |        MainSeries | _seriesId |   KREF |
; |      0 |     0 |       1 |      LineToolText |    IMJvUT |   KREF |
; |      0 |     0 |       2 |  LineToolHorzLine |    Uncxcm |   KREF |
; |      0 |     0 |       3 | LineToolTrendLine |    EVlZNG |    DAR |
; |      0 |     0 |       4 |      study_Volume |    62PMsu |        |
; |      0 |     1 |       0 |             Study |    yUjbO1 |        |
; |      1 |     0 |       0 |        MainSeries | _seriesId |    DAR |
; |      1 |     0 |       1 |      LineToolText |    IMJvUT |   KREF |
; |      1 |     0 |       2 |  LineToolHorzLine |    Uncxcm |   KREF |
; |      1 |     0 |       3 | LineToolTrendLine |    EVlZNG |    DAR |
; |      1 |     0 |       4 |      study_Volume |    62PMsu |        |




(def pane-sources-path [:charts 0 :panes 0 :sources])

(->> (load-chart env {:chart-id "1770419848"})
     (modify-chart {:chart-id "indicatortext"
                    :name "indicatortext"
                    :symbol "KO",
                    :short_name "KO",
                    :legs [{:symbol "KO", :pro_symbol "us:KO"}]})
     (set-chart-asset {:asset "KO"})
     (add-drawing {:type "LineToolText"
                   :asset "KO"
                   :interval "1D"
                   :state {:text "wunderbar"}
                   :points [{:time_t 1764838800,
                             :offset 0,
                             :price 70.506791517928498,
                             :interval "1D"}]})
     (save-chart env {}))




(-> (load-chart env {:chart-id "indicatortext"})
    (describe-charts)
    (print-table))

(defn is-main [x]
  ;(println "x: " x)
  (= (:type x) "MainSeries"))



(->> (load-chart env {:chart-id "1770419848"})
       ;(specter/transform pane-sources-path (fn [v] (conj (or v []) {:BONGO "3"})))
     (specter/select [:charts specter/ALL :panes specter/ALL :sources specter/ALL (specter/pred is-main)])
     (map #(select-keys % [:name :id :type :symbol :asset :state]))
     (map :state)
     (map #(select-keys % [:symbol :shortName])))



(def events
  (-> (tc/dataset "2018-pl2.csv" {:key-fn keyword})
      (tc/select-columns [:date-instant :asset :close :trailing-high
                          :trailing-high-date])
      (tc/rename-columns {:date-instant :date})
      (tc/rows :as-maps)))

events





(defn create-chart-for-event [{:keys [date asset close
                                      trailing-high trailing-high-date]}]

  (->> (load-chart env {:chart-id "1770419848"})
       (modify-chart {:chart-id "eventtest"
                      :name "eventtest"
                      :symbol asset
                      :short_name asset
                      :legs [{:symbol asset, :pro_symbol (str "us:" asset)}]})
       (set-chart-asset {:asset asset})
       (keep-only-main-chart)
       (remove-drawings)
       (add-drawing {:type "LineToolText"
                     :asset asset
                     :interval "1D"
                     :state {:text "wunderbar"}
                     :points [{:time_t date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (add-drawing {:type "LineToolVertLine"
                     :asset asset
                     :interval "1D"
                     :state {:text "wunderbar"}
                     :points [{:time_t date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (add-drawing {:type "LineToolVertLine"
                     :asset asset
                     :interval "1D"
                     :state {:text "wunderbar"}
                     :points [{:time_t trailing-high-date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (add-drawing {:type "LineToolHorzRay"
                     :asset asset
                     :interval "1D"
                     :state {:text "wunderbar"}
                     :points [{:time_t trailing-high-date
                               :offset 0
                               :price trailing-high
                               :interval "1D"}]})
       (set-axes-sources)
       (save-chart env {})))


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



(->> (load-chart env {:chart-id "eventtest"})
     (specter/select [:charts 0 :panes 0 :rightAxisesState 0 :sources]))
    ; [["IMJvUT" "Uncxcm" "EVlZNG" "_seriesId" "dEAF5W" "AGgSlg" "GPQTYy" "iCdF3b"]]

(->> (load-chart env {:chart-id "eventtest"})
     (specter/select [:charts 0 :panes 0 :sources specter/ALL :type]))
["MainSeries" "LineToolText" "LineToolVertLine" "LineToolVertLine" "LineToolHorzRay"]

(->> (load-chart env {:chart-id "eventtest"})
     (specter/select [:charts 0 :panes 0 :sources specter/ALL :id]))
["_seriesId" "dEAF5W" "AGgSlg" "GPQTYy" "iCdF3b"]

(->> (load-chart env {:chart-id "eventtest"})
     set-axes-sources
     (specter/select [:charts 0 :panes 0 :rightAxisesState 0 :sources]))