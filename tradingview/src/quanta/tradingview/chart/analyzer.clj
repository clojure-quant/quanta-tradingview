(ns quanta.tradingview.chart.analyzer
  (:require
   [nano-id.core :refer [nano-id]]
   [modular.persist.protocol :refer [save loadr]]
   [modular.persist.edn] ; side effects
   ))

(defn describe-sources [chart-idx pane-idx pane]
  (map-indexed
   (fn [source-idx source]
     {:chart chart-idx
      :pane pane-idx
      :source source-idx
      :type (:type source)
      :id (:id source)})
   (:sources pane)))

(defn describe-panes [chart chart-idx]
  (->> (:panes chart)
       (map-indexed
        (fn [pane-idx pane]
          (println "describe panes" chart-idx pane-idx)
          (describe-sources chart-idx pane-idx pane)))
       (apply concat)))

(defn describe-charts
  "input: a loaded chart data,
   output: a seq of chart/pane/source description
   contains all sources for all charts and all panes
   useful to be printed as a table."
  [chart-data]
  (->> (:charts chart-data)
       (map-indexed
        (fn [chart-idx chart]
          (describe-panes chart chart-idx)))
       (apply concat)))

(defn get-source [chart-data chart-idx pane-idx source-idx]
  (let [chart (get (:charts chart-data) chart-idx)
        pane (get   (:panes chart) pane-idx)
        source (get  (:sources pane) source-idx)]
    source))

(defn save-source
  [source]
  (let [path "./resources/tradingview/template/"
        filename (str path (:type source) ".edn")]
    (save :edn filename source)))


(defn create-source
  [{:keys [type asset interval points]}]
  (let [path "./resources/tradingview/template/"
        filename (str path type ".edn")]
    (-> (loadr :edn filename)
        (assoc :id (nano-id 6)
               :linkKey (nano-id 12)
               :points points)
        (assoc-in [:state  :symbol] asset)
        (assoc-in [:state  :interval] interval))))

(defn pane [data chart-idx pane-idx]
  "data is a loaded chart 
   returns the a pane (with index pane-idx) for chart (with index chart-idx)"
  (-> data :charts (get chart-idx) :panes (get pane-idx)))

(defn pane-owner
  "returns the id of the mainseries or the first study in the pane.
   this is needed because drawings need to link to this id."
  [data chart-idx pane-idx]
  (let [p (pane data chart-idx pane-idx)
        main (->> p
                  :sources
                  (filter (fn [source]
                            (= (:type source) "MainSeries")))
                  first
                  :id)
        study (->> p
                   :sources
                   (filter (fn [source]
                             (= (:type source) "Study")))
                   first
                   :id)]
    (or main study)))

(defn add-templates
  "data is a loaded chart 
   all drawings contained in the chart will be saved as a template"
  [data]
  (let [rows  (->> (describe-charts data)
                   (remove (fn [row] (= (:type row) "MainSeries")))
                   (remove (fn [row] (= (:type row) "Study"))))]
    (doall (map (fn [row]
                  (-> data
                      (get-source (:chart row) (:pane row) (:source row))
                      (save-source))) rows))))


(defn keep-only-main-chart [data]
  (let [chart-0 (-> data :charts (get 0))
        pane-0-0 (pane data 0 0)
        chart-0-pane-0 (assoc chart-0 :panes [pane-0-0])]
    (assoc data :charts [chart-0-pane-0])))


(defn remove-pane-drawings [pane]
  (let [sources (->> (:sources pane)
                     (filter (fn [source]
                               (or (= (:type source) "MainSeries")
                                   (= (:type source) "Study"))))
                     (into []))]
    (assoc pane :sources sources)))

(defn remove-chart-drawings [chart]
  (let [panes (->> (:panes chart)
                   (map remove-pane-drawings)
                   (into []))]
    (assoc chart :panes panes)))

(defn remove-drawings [data]
  (let [charts (->> (:charts data)
                    (map remove-chart-drawings)
                    (into []))]
    (assoc data :charts charts)))

; | :chart | :pane | :source |               :type |       :id |
; |--------+-------+---------+---------------------+-----------|
; |      0 |     0 |       0 |          MainSeries | _seriesId |