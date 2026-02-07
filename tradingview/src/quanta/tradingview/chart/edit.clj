(ns quanta.tradingview.chart.edit
  (:require
   [babashka.fs :as fs]
   [nano-id.core :refer [nano-id]]
   [com.rpl.specter :as specter]
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

;ls resources/tradingview/template/
;LineToolArrowMarker.edn  LineToolCrossLine.edn  LineToolHorzRay.edn  LineToolTable.edn  LineToolText.edn  LineToolTrendLine.edn  LineToolVertLine.edn

(def template-path "./resources/tradingview/template/")

(defn- remove-ext [s]
  (subs s 0 (- (count s) 4)))


(defn list-sources []
  (->> (fs/list-dir template-path "*.edn")
       (map fs/file-name)
       (map remove-ext)))

(defn save-source
  [source-data]
  (let [filename (str template-path (:type source-data) ".edn")]
    (save :edn filename source-data)))

(defn load-source
  [source-type]
  (let [filename (str template-path source-type ".edn")]
    (if (fs/exists? filename)
      (loadr :edn filename)
      (throw (ex-info (str "source not found: " filename)
                      {:source-type source-type
                       :filename filename})))))

(def tools-dict
  ; in state nil means mandatory
  {"LineToolText" {:state {:text ""} :points 1}
   "LineToolTrendLine" {:state {:text ""} :points 2}
   "LineToolTable" {:state {:cells nil} :points 1}
   "LineToolVertLine" {:state {:text ""
                               :title ""} :points 1}})

(defn mandatory-state-fields [state-schema]
  (->> state-schema
       :state
       (remove (fn [[k v]] v))
       keys))


;(mandatory-state-fields (get tools-dict "LineToolText"))
;(mandatory-state-fields (get tools-dict "LineToolTable"))

(defn create-source
  [{:keys [type asset interval points state]}]
  (assert type ":type must be the type of the the drawing as string, example: LineToolText")
  (assert asset ":asset must be the symbol of the asset as string, example: SPY")
  (assert interval ":interval must me the interval as string, example: 1D")
  (assert points ":points must be a vector of points")
  (assert (vector? points) ":points must be a VECTOR of points")
  (assert (or (not state) (map? state)) ":state must be a map or nil")
  (let [drawing-spec (get tools-dict type)
        _ (assert drawing-spec (str type " does not contain a spec definition. "))
        point-count (:points drawing-spec)
        state-schema (:state drawing-spec)
        mandatory-fields (mandatory-state-fields drawing-spec)
        state-fields (select-keys state (keys state-schema))]
    (when mandatory-fields
      (assert state (str type " must have :state {} with all mandatory keys " mandatory-fields))
      (doall (map (fn [k]
                    (assert (get state k) (str type " must have :state " k))) mandatory-fields)))
    (assert (= (count points) point-count) (str ":points must be a vector with " point-count " points"))
    (let [data (load-source type)
          data-state (-> data
                         :state
                         (assoc :symbol asset :interval interval)
                         (merge state-fields)
                         )]
      (assoc data
             :id (nano-id 6)
             :linkKey (nano-id 12)
             :points points
             :state data-state))))

(defn modify-chart [opts chart-data]
  (merge chart-data opts))


(defn add-drawing  [ {:keys [type asset interval points state
                                       chart-idx pane-idx
                                       ]
                                :or {chart-idx 0
                                     pane-idx 0}
                                :as opts} chart-data]
  (let [source (create-source opts)
        axis-sources-path [:charts chart-idx :panes pane-idx :rightAxisesState 0 :sources]
        source-id (:id source)
        pane-sources-path [:charts chart-idx :panes pane-idx :sources]
        ]
    (println "pane-sources-path " pane-sources-path)
    (->> chart-data 
         ; add drawing to pane
         (specter/transform pane-sources-path (fn [v] (conj (or v []) source)))
         ; add drawing to right-axes source list
         (specter/transform axis-sources-path (fn [v] (conj (or v []) source-id)))
         )
    
    ))


(defn add-source [m new-source]
  (specter/transform sources-path
                     (fn [v] (conj (or v []) new-source))
                     m))

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


