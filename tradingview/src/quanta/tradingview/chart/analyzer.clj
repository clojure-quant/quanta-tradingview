(ns quanta.tradingview.chart.analyzer
  (:require
   [nano-id.core :refer [nano-id]]
   [differ.core :as differ]
   [modular.persist.protocol :refer [save loadr]]
   [modular.persist.edn] ; side effects
   [quanta.tradingview.chart :refer [save-chart chart-list load-chart]]
   ;[ta.tradingview.chart.template :refer [save-template]]
   ))

;; extract

(defn get-pane [chart]
  (-> chart
      :charts first :panes first))

(defn get-sources [chart]
  (-> chart
      :charts first :panes first :sources))



(defn filter-type [chart t]
  (let [sources (get-sources chart)]
    (-> (filter #(= t (:type %)) sources)
        first)))

(defn source-state-summary [source]
  (select-keys (:state source) [:shortName :symbol]))

(defn keys-sorted [o]
  (-> o keys sort))

#_(defn diff-summary [preprocess id-generated id-compare]
    (let [g (-> (load-chart 77 77 id-generated) preprocess)
          c (-> (load-chart 77 77 id-compare) preprocess)]
      {:l (differ/diff g c)
       :r (differ/diff c g)}))

#_(defn extract-study [user-id client-id chart-id type name]
    (let [save #(save-template name %)]
      (-> (load-chart user-id client-id chart-id)
          (filter-type type)
          (save))))

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
   "
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
  (-> data :charts (get chart-idx) :panes (get pane-idx)))

(defn pane-owner [data chart-idx pane-idx]
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

(defn add-templates [data]
  (let [rows  (->> (describe-charts data)
                   (remove (fn [row] (= (:type row) "MainSeries")))
                   (remove (fn [row] (= (:type row) "Study"))))]
    (doall (map (fn [row]
                  (-> data 
                      (get-source (:chart row) (:pane row) (:source row))    
                      (save-source))) rows))
    ))




(comment

  (extract-study 77 77 1636805136 "LineToolGannComplex" "gann-nice")

  (extract-study 77 77 1636839899 "LineToolFibCircles" "fib-circle")

  (def id-generated 123)
  (def id-compare 1636726545)



  ; test: sources list
  (-> (load-chart 77 77 id-generated) sources-summary)
  (-> (load-chart 77 77 id-compare)   sources-summary)

  ; test: mainseries keys
  (-> (load-chart 77 77 id-generated)       (filter-type "MainSeries") keys-sorted)
  (-> (load-chart 77 77 id-compare)  (filter-type "MainSeries") keys-sorted)

   ; test: mainseries state
  (-> (load-chart 77 77 id-generated)       (filter-type "MainSeries") source-state-summary)
  (-> (load-chart 77 77 id-compare)  (filter-type "MainSeries") source-state-summary)

; test mainseries differences (in both ways)
  (differ/diff
   (-> (load-chart 77 77 id-generated) (filter-type "MainSeries"))
   (-> (load-chart 77 77 id-compare)  (filter-type "MainSeries")))

  (differ/diff
   (-> (load-chart 77 77 id-compare)  (filter-type "MainSeries"))
   (-> (load-chart 77 77 id-generated) (filter-type "MainSeries")))

; test study differences (in both ways)
  (differ/diff
   (-> (load-chart 77 77 id-generated) (filter-type "Study"))
   (-> (load-chart 77 77 id-compare)  (filter-type "Study")))

  (differ/diff
   (-> (load-chart 77 77 id-compare)  (filter-type "Study"))
   (-> (load-chart 77 77 id-generated) (filter-type "Study")))

    ; test study differences (in both ways)
  (diff-summary
   #(dissoc % :id :charts :symbol_type :exchange :timestamp :symbol :name :short_name :publish_request_id :legs)
   id-generated id-compare)

  ;; no differences in mainseries
  (diff-summary
   #(filter-type % "MainSeries")
   id-generated id-compare)

  ;; no differences in study (except for ids)
  (diff-summary
   #(filter-type % "Study")
   id-generated id-compare)

  ;; no differences in except ids of sources
  (diff-summary
   #(-> (get-pane %) (dissoc :sources))
   id-generated id-compare)

  (diff-summary
   #(dissoc % :symbol :name :short_name :publish_request_id :legs :id :exchange :timestamp :symbol_type)
         ;identity
   id-generated id-compare)

  (-> (load-chart 77 77 1636558275) ; AMZN: Pitchfork   MSFT: LineTrend
      :content  ; :layout :charts
      :charts
      first
      :panes
      first ; (:sources :leftAxisSources  :rightAxisSources :leftAxisState :rightAxisState  :overlayPriceScales :mainSourceId)
   ; :sources
      :sources
    ;count
      (get 5)
    ;:type
    ;(get-in [:state :styles])
      )

;  
  )