(ns demo.import-templates
  (:require
   [quanta.tradingview.chart.db :refer [load-chart]]
   [quanta.tradingview.chart.source :refer [save-source list-sources load-source create-source]]
   [quanta.tradingview.chart.source-import :refer [add-templates]] 
   [demo.env :refer [env]]))

;; play with existing sources
(list-sources)

(load-source "LineToolText")
(create-source {:type "LineToolTrendLine"
                :asset :KO
                :interval "1D"
                :points [{} {}]})

(create-source {:type "LineToolText"
                :asset :KO
                :interval "1D"
                :points [{}]})

(create-source {:type "LineToolTable"
                :asset :KO
                :interval "1D"
                :points [{}]
                :state {:cells []}})

(create-source {:type "LineToolCrossLine"
                :asset :KO
                :interval "1D"
                :points [{}]})

;; add new sources

(-> (load-chart env {:chart-id "1770323667"})
    (add-templates))

(-> (load-chart env {:chart-id "eventtest"})
    (add-templates))



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
