(ns quanta.tradingview.chart.edit
  (:require
   [nano-id.core :refer [nano-id]]
   [com.rpl.specter :as specter]
   [quanta.tradingview.chart.source :refer [create-source source-type-study]] 
   ))

(defn describe-sources [chart-idx pane-idx pane]
  (map-indexed
   (fn [source-idx source]
     {:chart chart-idx
      :pane pane-idx
      :source source-idx
      :type (:type source)
      :id (:id source)
      :asset (get-in source [:state :symbol])
      })
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


(defn modify-chart [opts chart-data]
  (merge chart-data opts))

(defn pane [chart-data chart-idx pane-idx]
  "data is a loaded chart 
   returns the a pane (with index pane-idx) for chart (with index chart-idx)"
  (-> chart-data :charts (get chart-idx) :panes (get pane-idx)))

(defn pane-owner
  "returns the id of the mainseries or the first study in the pane.
   this is needed because drawings need to set their :ownerSource to this id."
  [chart-data chart-idx pane-idx]
  (let [p (pane chart-data chart-idx pane-idx)
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

(defn add-drawing  [{:keys [type asset interval points state
                            chart-idx pane-idx]
                     :or {chart-idx 0
                          pane-idx 0}
                     :as opts} chart-data]
  (let [owner-source-id (pane-owner chart-data chart-idx pane-idx)
        source (-> (create-source opts)
                   (assoc :ownerSource owner-source-id))
        axis-sources-path [:charts chart-idx :panes pane-idx :rightAxisesState 0 :sources]
        source-id (:id source)
        pane-sources-path [:charts chart-idx :panes pane-idx :sources]]
    ;(println "pane-sources-path " pane-sources-path)
    (->> chart-data
         ; add drawing to pane
         (specter/transform pane-sources-path (fn [v] (conj (or v []) source)))
         ; add drawing to right-axes source list
         (specter/transform axis-sources-path (fn [v] (conj (or v []) source-id))))))



(defn keep-only-main-chart [chart-data]
  (let [chart-0 (-> chart-data :charts (get 0))
        pane-0-0 (pane chart-data 0 0)
        chart-0-pane-0 (assoc chart-0 :panes [pane-0-0])]
    (assoc chart-data :charts [chart-0-pane-0])))


(defn remove-pane-drawings [pane]
  (let [sources (->> (:sources pane)
                     (filter (fn [source]
                               (contains? source-type-study (:type source))))
                     (into []))]
    (assoc pane :sources sources)))

(defn remove-chart-drawings [chart1]
  (let [panes (->> (:panes chart1)
                   (map remove-pane-drawings)
                   (into []))]
    (assoc chart1 :panes panes)))

(defn remove-drawings [chart-data]
  (let [charts (->> (:charts chart-data)
                    (map remove-chart-drawings)
                    (into []))]
    (assoc chart-data :charts charts)))

; | :chart | :pane | :source |               :type |       :id |
; |--------+-------+---------+---------------------+-----------|
; |      0 |     0 |       0 |          MainSeries | _seriesId |

(defn is-main [x]
  (= (:type x) "MainSeries"))

(defn set-chart-asset
  "sets the asset of a chart, chart 0 if no chart-id specified"
  [{:keys [asset chart-id]
    :or {chart-id 0}} chart-data]
  (let [path [:charts chart-id :panes specter/ALL :sources specter/ALL (specter/pred is-main) :state]]
    (specter/transform path (fn [v]
                              (assoc v :symbol asset
                                     :shortName asset))
                       chart-data)))

(defn set-axes-sources
  ([chart-data]
   (set-axes-sources {} chart-data))
  ([{:keys [chart-id pane-id]
     :or {chart-id 0
          pane-id 0}} chart-data]
   (let [; we do not want to include volume study in the right axis, as this would
         ; fuck up the axis specification. But both MainSeries, Studies (Indicators) 
         ; and Drawings can be included.
         sources (specter/select [:charts chart-id :panes pane-id :sources specter/ALL 
                                  (specter/pred #(not (= "study_Volume" (:type %))))
                                  :id] chart-data)]
     (specter/setval [:charts chart-id :panes pane-id :rightAxisesState 0 :sources]
                     sources chart-data))))