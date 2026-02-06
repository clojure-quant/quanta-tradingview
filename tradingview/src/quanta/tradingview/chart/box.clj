(ns quanta.tradingview.chart.box
  (:require
   ;[clojure.set :refer [rename-keys]]
   [clojure.walk]
   [taoensso.timbre :refer [debug info warnf]]
   [cheshire.core :refer [parse-string generate-string]]
   ;[schema.core :as s]
   [tick.core :as tick]
   [cljc.java-time.instant :as ti]
   [clojure.java.io :as io]
   [babashka.fs :refer [create-dirs]]
   [modular.persist.edn] ; side effects to load edn files
   [modular.persist.protocol :refer [save loadr]]
   ;[modular.helper.id :refer [guuid-str]]
   [quanta.tradingview.chart.db :refer [load-chart save-chart]]))

(defn chart-unbox [{:keys [content _client _id _user] :as data}]
  (let [data-without-content (dissoc data :content :chart
                                     "content" "chart")
        content (or content (get data "content"))
        ;(:client :user "name" "content" "symbol" "resolution")

        content-edn (parse-string content true)
        {:keys [legs content]} content-edn
        ; content-edn
        ; {:content :legs :id
        ;  :symbol :name :symbol_type :exchange :listed_exchange :short_name :is_realtime :resolution 
        ;  :publish_request_id :description}
        chart-meta (dissoc content-edn :content)
        content-unboxed  (parse-string content true) ; {:layout :charts}
        legs-unboxed (into [] (parse-string legs true))]
    (info "chart-unbox: data keys:" (keys data))
    ;(:client :user "name" "content" "symbol" "resolution")
    (info "data no content: " data-without-content)
    (info "content-edn keys" (keys content-edn))
    ;(:description :charts_symbols :content :listed_exchange :symbol :name :is_realtime :short_name :resolution :legs :exchange :symbol_type)
    (info "content-unboxed keys" (keys content-unboxed))
    ;(:symbolLock :trackTimeLock :layout :name :dateRangeLock :layoutsSizes :intervalLock :crosshairLock :charts)
    (merge data-without-content
           chart-meta
           {:legs legs-unboxed}
           content-unboxed)))

;chart-unbox: data keys: (:client :user :chart :name :content :symbol :resolution)
;wrapper:  (:client :user :name :symbol :resolution)
;content-edn keys (:description :content :listed_exchange :symbol :name :is_realtime :short_name :resolution :legs :id :exchange :symbol_type)
;content-unboxed keys (:symbolLock :trackTimeLock :layout :name :dateRangeLock :layoutsSizes :intervalLock :crosshairLock :charts)


(defn chart-box [{:keys [id name timestamp layout charts legs] :as data}]
  (let [chart {:layout layout
               :charts charts}
        overview (-> (select-keys data [:description :listed_exchange :symbol :name :is_realtime
                                        :short_name :publish_request_id :resolution :exchange
                                        :symbol_type])
                     (assoc :legs (generate-string legs)
                            :content (generate-string chart)))]
    {:id id
     :name name
     :timestamp (float timestamp)
     :content (generate-string overview)}))

;; chart


(defn filename-chart-unboxed  [charts-path client-id user-id chart-id]
  (str charts-path "boxed_chart_" client-id "_" user-id "_" chart-id ".edn"))

(def debug? false)

(defn save-chart-boxed
  [charts-path client-id user-id chart-id data-boxed]
  ;(info "save-chart-boxed: " data-boxed)
  (let [{:keys [content]} data-boxed
        data-edn (chart-unbox data-boxed)]
    (when debug?
      (save :edn (filename-chart-unboxed charts-path client-id user-id chart-id) data-boxed))
    (save-chart charts-path client-id user-id chart-id data-edn)))

(defn load-chart-boxed [charts-path client-id user-id chart-id]
  (let [data (load-chart charts-path client-id user-id chart-id)
        data-boxed (chart-box data)]
    (dissoc data-boxed :client :user)))





(comment

  (split-filename "chart_77_77_1636524198.edn")
  (split-filename ".placeholder")
  (explore-dir "./tv/chart")

;  
  )
