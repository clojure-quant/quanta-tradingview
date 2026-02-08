(ns quanta.tradingview.chart.source-import
  (:require
   [quanta.tradingview.chart.source :refer [save-source source-type-study]]
   [quanta.tradingview.chart.edit :refer [describe-charts get-source]]))


(defn add-templates
  "data is a loaded chart 
   all drawings contained in the chart will be saved as a template"
  [data]
  (let [rows  (->> (describe-charts data)
                   (remove (fn [row] (contains? source-type-study (:type row)))))]
    (doall (map (fn [row]
                  (-> data
                      (get-source (:chart row) (:pane row) (:source row))
                      (save-source))) rows))))

