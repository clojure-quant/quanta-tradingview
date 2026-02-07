(ns demo.chart-analysis
  (:require
   [clojure.pprint :refer [print-table]]
   [quanta.tradingview.chart.db :refer [save-chart chart-list load-chart]]
   [quanta.tradingview.chart.edit :refer [describe-charts get-source describe-sources
                                          save-source create-source
                                          pane pane-owner add-templates keep-only-main-chart
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
; |      0 |     1 |       0 |               Study |    EfnuL8 |
; |      0 |     1 |       1 |   LineToolTrendLine |    cbmSrG |

(-> (load-chart env {:chart-id "1770323667"})
    (keep-only-main-chart)
    (describe-charts)
    (print-table))

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
    (get-source 0 0 6)
    (save-source))

(-> (load-chart env {:chart-id "1770323667"})
    (add-templates))




(create-source "LineToolTrendLine" "MO" "5D")

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


(-> (load-chart env {:chart-id "1770323667"})
    (add-templates)
    (print-table))


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


"LineToolVertLine"


; vert line
:type "LineToolVertLine",
:id "k5ScMA",
:ownerSource "_seriesId", ; study or seriesId (the series in the pane.)
:linkKey "PkPEA06MeEBJ",
:state :symbol "US:KO",
:interval "1D",
:points [{:time_t 1610096400,
          :offset 0,
          :price 67.13970181128232}],

; horizontal ray
:type "LineToolHorzRay",
:id "k8HPzJ",
:ownerSource "_seriesId", ; study or seriesId (the series in the pane.)
:linkKey "M52wAMEGLzfL",
:points [{:time_t 1650531600,
          :offset 0,
          :price 67.06831592075459}],

; cross
:type "LineToolCrossLine",
:id "niL0xL",
:ownerSource "_seriesId",
:linkKey "3XiV44bHTztK",
:state :symbol "KO"
:interval "1D"

