(ns demo.eventstudy
  (:require
   [clojure.pprint :refer [print-table]]
   [com.rpl.specter :as specter]
   [quanta.tradingview.chart.db :refer [save-chart chart-list load-chart]]
   [quanta.tradingview.chart.edit :refer [describe-charts get-source describe-sources
                                              save-source 
                                              pane pane-owner add-templates keep-only-main-chart
                                              remove-drawings
                                              list-sources load-source create-source add-drawing
                                          modify-chart set-chart-asset
                                          ]]
   [demo.env :refer [env]]))


(-> (load-chart env {:chart-id "1770419848"})
    (describe-charts)
    (print-table))

; | :chart | :pane | :source |             :type |       :id |
; |--------+-------+---------+-------------------+-----------|
; |      0 |     0 |       0 |        MainSeries | _seriesId |
; |      0 |     0 |       1 |      LineToolText |    IMJvUT |
; |      0 |     0 |       2 |      study_Volume |    62PMsu |
; |      0 |     0 |       3 |  LineToolHorzLine |    Uncxcm |
; |      0 |     0 |       4 | LineToolTrendLine |    EVlZNG |
; |      0 |     1 |       0 |             Study |    yUjbO1 |
; |      1 |     0 |       0 |        MainSeries | _seriesId |
; |      1 |     0 |       1 |      LineToolText |    IMJvUT |
; |      1 |     0 |       2 |  LineToolHorzLine |    Uncxcm |
; |      1 |     0 |       3 |      study_Volume |    62PMsu |
; |      1 |     0 |       4 | LineToolTrendLine |    EVlZNG |

(->> (load-chart env {:chart-id "1770419848"})
    #_(assoc :id "event-001"
           :name "EventStudy-001"
           :short_name "MMM"
           :symbol "MMM"
           :legs [{:symbol "MMM", :pro_symbol "us:MMM"}]
           
           )
    (specter/select [:charts specter/ALL :panes specter/ALL :rightAxisesState 0 :sources])
    ) 
    ; [["IMJvUT" "_seriesId" "Uncxcm" "EVlZNG"]]

(specter/transform )

(list-sources)

(load-source "LineToolText")
(create-source {:type "LineToolTrendLine"
                :asset :KO
                :interval "1D"
                :points [{} {}]
                })

(create-source {:type "LineToolText"
                :asset :KO
                :interval "1D"
                :points [{}]})

(def pane-sources-path [:charts 0 :panes 0 :sources])

 (->> (load-chart env {:chart-id "1770419848"})
      (modify-chart {:chart-id "indicatortext"
                     :name "indicatortext"
                     :symbol "KO",
                     :short_name "KO",
                     :legs [{:symbol "KO", :pro_symbol "us:KO"}],
                     })
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
  (println "xcoutn " (count x))
  (= (:type x) "MainSeries"))

(even? 3 )

  (->> (load-chart env {:chart-id "1770419848"})
       ;(specter/transform pane-sources-path (fn [v] (conj (or v []) {:BONGO "3"})))
       (specter/select [:charts specter/ALL :panes specter/ALL :sources specter/ALL (specter/pred is-main)])
       (map #(select-keys % [:name :id :type :symbol :asset :state] ))
       (map :state)
       (map #(select-keys % [:symbol :shortName]))
       
       
       
       
       )
 
    

        
     
    


(create-source {:type "LineToolTable"
                :asset :KO
                :interval "1D"
                :points [{}]
                :state {:cells []}
                })



(create-source {:type "LineToolCrossLine"
                :asset :KO
                :interval "1D"
                :points [{}]})

(add-indicator "LineToolTrendLine")

; trendline
:type "LineToolTrendLine",
:id "cbmSrG",
:ownerSource "EfnuL8", ; study or seriesId (the series in the pane.)
:state  :symbol "KO",
:interval "1D",
:points [{:time_t 1715590800,
          :offset 0,
          :price 90.37129883831498,
          :interval "1D"}
         {:time_t 1736845200,
          :offset 0,
          :price 59.58837955966041,
          :interval "1D"}],


10:    (specter/setval path v state)))
118:  (specter/select [0 :b :c] data)
119:  (specter/setval [0 :b :c] 555 data)
121:  (specter/setval [0 :b :c] 555 [])
123:  (specter/select [0 :b :c] data)
135:  (specter/setval [:a specter/ALL] 4 data)
137:  (specter/transform [0 :b :c]
138:                     specter/NONE
141:  (specter/select [:a :b] data)
143:  (specter/setval [1 :asset]  "NZD/USD"

0-0-0 :state :symbol "KREF",
             :shortName "KREF",
0-0-1 :state :symbol "OMC"
             :text "afdsasdfasdfasdfs asdfasdf"
      :points [{:time_t 1757322000,
                :offset 0,
                :price 72.14071136999496,
                :interval "1D"}],

0-0-2 :state :symbol "KREF",
             :text "kkr real estate finance trust"
      :points [{:time_t 1752742800,
                :offset 0,
                :price 8.295988605536083,
                :interval "1D"}],
       :rightAxisesState   :sources ["IMJvUT"
                                    "_seriesId"
                                    "Uncxcm"]