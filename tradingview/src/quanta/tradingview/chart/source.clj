(ns quanta.tradingview.chart.source
  (:require
   [babashka.fs :as fs]
   [nano-id.core :refer [nano-id]]
   [tick.core :as t]
   [com.rpl.specter :as specter]
   [modular.persist.protocol :refer [save loadr]]
   [modular.persist.edn] ; side effects
   ))

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
                               :title ""} :points 1}
   "LineToolHorzRay" {:state {:text ""
                              :title ""} :points 1}})

;https://saveload.tradingview.com/1.1/drawing_templates?client=trading_platform_demo&user=public_user&tool=LineToolHorzRay
;https://saveload.tradingview.com/1.1/drawing_templates?client=trading_platform_demo&user=public_user&tool=LineToolTrendLine

; {"status": "ok", "data": ["Choch", "\u538b\u529b\u7ebf", "Bull trendline", "W", "H4", "uptrend", "Lik", "white line", "rish_draw", "Lg", "rishabh_drawing2", "tl", "3reere", "tla"]}%   

(defn mandatory-state-fields [state-schema]
  (->> state-schema
       :state
       (remove (fn [[k v]] v))
       keys))


;(mandatory-state-fields (get tools-dict "LineToolText"))
;(mandatory-state-fields (get tools-dict "LineToolTable"))

(defn ->epoch [dt]
  (-> dt
      (t/instant)
      (.getEpochSecond)))

(defn date->epoch [dt]
  (-> dt
      (t/at (t/time "00:00:00"))
      (t/in (t/zone "UTC"))
      (->epoch)))

(-> (t/date)
    (t/at (t/time "00:00:00"))
    (t/in (t/zone "UTC"))
    (t/instant)
    )

(defn sanitize-point [{:keys [time_t price] :as point}]
  (cond 
    ; integer - leave unchanged 
    (number? time_t) 
    point
    ; date-time - convert to epoch
    (or (t/zoned-date-time? time_t) (t/instant? time_t))
    (update point :time_t ->epoch)
    ; date 
    (t/date? time_t)
    (update point :time_t date->epoch)
    ; default - leave unchanged
    :else     
    point))

(defn sanitize-points [points]
  (->> (map sanitize-point points)
       (into [])))

(def source-type-study #{"MainSeries" "Study" "study_Volume"})


(comment
  (date->epoch (t/date))
  (->epoch (t/instant))

  (sanitize-point {:time_t (t/date "2023-01-30") :price 5})
  (sanitize-points [{:time_t (t/date "2022-01-30") :price 50}
                    {:time_t (t/date "2023-01-30") :price 100}])
  ;
  )

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
                         (merge state-fields))]
      (assoc data
             :id (nano-id 6)
             :linkKey (nano-id 12)
             :points (sanitize-points points)
             :state data-state))))