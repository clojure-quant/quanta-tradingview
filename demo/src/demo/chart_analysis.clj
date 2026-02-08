(ns demo.chart-analysis
  (:require
   [clojure.pprint :refer [print-table]]
      [com.rpl.specter :as specter]
   [quanta.tradingview.chart.db :refer [save-chart chart-list load-chart]]
   [quanta.tradingview.chart.edit :refer [describe-charts get-source describe-sources
                                          pane pane-owner keep-only-main-chart
                                          remove-drawings]]
   [demo.env :refer [env]]))

(-> (chart-list env)
    (print-table))

; |       :name | :symbol | :resolution |        :id |  :timestamp |
; |-------------+---------+-------------+------------+-------------|
; |  KO-FLORIAN |      KO |          1D | 1770323667 | 1.7703885E9 |
; |       fffff |     OMC |          1D | 1770336868 | 1.7703369E9 |
; |         yyy |      AA |          1D | 1770300727 | 1.7703007E9 |
; |       hello |     ASR |          1D | 1770336627 | 1.7703366E9 |
; |         flo |      AA |          1D | 1770300453 | 1.7703004E9 |
; |       bongo |     SYK |          1D | 1770336445 | 1.7703364E9 |
; |         flo |     EIX |          1D | 1770336553 | 1.7703365E9 |
; | 3-indicator |     DAR |          1D | 1770334643 | 1.7703346E9 |


(-> (load-chart env {:chart-id "1770323667"})
    (describe-charts)
    (print-table))

; | :chart | :pane | :source |               :type |       :id | :asset |
; |--------+-------+---------+---------------------+-----------+--------|
; |      0 |     0 |       0 |          MainSeries | _seriesId |     KO |
; |      0 |     0 |       1 |     LineToolHorzRay |    k8HPzJ |  US:KO |
; |      0 |     0 |       2 |    LineToolVertLine |    c56ymk |  US:KO |
; |      0 |     0 |       3 |    LineToolVertLine |    k5ScMA |  US:KO |
; |      0 |     0 |       4 |   LineToolTrendLine |    cxJ4et |     KO |
; |      0 |     0 |       5 |     LineToolHorzRay |    Dq6XrD |     KO |
; |      0 |     0 |       6 |   LineToolCrossLine |    niL0xL |     KO |
; |      0 |     0 |       7 |        LineToolText |    Q7dHoz |     KO |
; |      0 |     0 |       8 | LineToolArrowMarker |    kQVlCl |     KO |
; |      0 |     0 |       9 |       LineToolTable |    HPnS7G |     KO |
; |      0 |     1 |       0 |               Study |    EfnuL8 |        |
; |      0 |     1 |       1 |   LineToolTrendLine |    cbmSrG |     KO |

(-> (load-chart env {:chart-id "1770585201"})
 ;(load-chart env {:chart-id "event-AIG-63"})
 (describe-charts)
 (print-table)
 )
; | :chart | :pane | :source |        :type |       :id | :asset |
; |--------+-------+---------+--------------+-----------+--------|
; |      0 |     0 |       0 |   MainSeries | _seriesId |    OMC |
; |      0 |     0 |       1 | study_Volume |    389Oub |        |
; |      0 |     0 |       2 |        Study |    Eex4ul |        |
; |      0 |     0 |       3 |        Study |    snTsLT |        |

(->> (load-chart env {:chart-id "1770585201"})
     (specter/select [:charts 0 :panes 0 :sources specter/ALL 
                     (specter/pred #(not (= "study_Volume" (:type %))))
                      :id])
     )
["_seriesId" "389Oub" "Eex4ul" "snTsLT"]
(->> (load-chart env {:chart-id "1770585201"})
     (specter/select [:charts 0 :panes 0 :rightAxisesState 0 :sources]))
[["_seriesId" "Eex4ul" "snTsLT"]]

;["_seriesId" "389Oub" "Eex4ul" "snTsLT" "VYONZ0"
;"s-uhgz" "hhTNy6" "qGauYr"]
; | :chart | :pane | :source |               :type |       :id |
; |--------+-------+---------+---------------------+-----------|
; |      0 |     0 |       0 |          MainSeries | _seriesId |
; |      0 |     0 |       1 |     LineToolHorzRay |    k8HPzJ |
; |      0 |     0 |       2 |    LineToolVertLine |    c56ymk |
; |      0 |     0 |       3 |    LineToolVertLine |    k5ScMA |
; |      0 |     0 |       4 |   LineToolTrendLine |    cxJ4et |
; |      0 |     0 |       5 |     LineToolHorzRay |    Dq6XrD |
; |      0 |     0 |       6 |   LineToolCrossLine |    niL0xL |
; |      0 |     0 |       7 |        LineToolText |    Q7dHoz |
; |      0 |     0 |       8 | LineToolArrowMarker |    kQVlCl |
; |      0 |     0 |       9 |       LineToolTable |    HPnS7G |


(-> (load-chart env {:chart-id "1770323667"})
    (get-source 0 0 3))

(-> (load-chart env {:chart-id "1770323667"})
    (get-source 0 0 6))





(create-source
 {:type "LineToolVertLine"
  :asset "MO"
  :interval  "5D"
  :points [{:time_t 1610096400,
            :offset 0,
            :price 67.13970181128232}]})

(-> (load-chart env {:chart-id "1770323667"})
    (pane-owner 0 0))
; "_seriesId"

(-> (load-chart env {:chart-id "1770323667"})
    (pane-owner 0 1))
; "EfnuL8"



(->> (load-chart env {:chart-id "1770323667"})
     (remove-drawings)
     (keep-only-main-chart)
     (save-chart env {:chart-id "999"}))

(-> (load-chart env {:chart-id "999"})
    (describe-charts)
    (print-table))

; | :chart | :pane | :source |      :type |       :id |
; |--------+-------+---------+------------+-----------|
; |      0 |     0 |       0 | MainSeries | _seriesId |


