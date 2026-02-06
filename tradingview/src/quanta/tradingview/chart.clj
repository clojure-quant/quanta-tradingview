(ns quanta.tradingview.chart
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
   
   ))

(defn now-epoch []
  (-> (tick/now)
      (ti/get-epoch-second)))

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

(defn filename-chart  [charts-path client-id user-id chart-id]
  (str charts-path "chart_" client-id "_" user-id "_" chart-id ".edn"))

(defn save-chart
  [charts-path client-id user-id chart-id data]
  (let [data (assoc data
                    :timestamp (now-epoch)
                    :id chart-id ;(if (string? chart-id)  (Integer/parseInt chart-id) chart-id)
                    :client client-id ; (if (string? client-id)  (Integer/parseInt client-id) client-id)
                    :user user-id ;(if (string? user-id)  (Integer/parseInt user-id) user-id)
                    )]
    (create-dirs charts-path)
    (save :edn (filename-chart charts-path client-id user-id chart-id) data)
    (info "saved chart id: " chart-id)))

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

(defn load-chart 
  ([charts-path client-id user-id chart-id]
   (println "charts-path: " charts-path " client-id: " client-id " user-id: " user-id " chart-id: " chart-id)
   (loadr :edn (filename-chart charts-path client-id user-id chart-id)))
  ([env opts]
   (let [charts-path (get-in env [:tradingview :charts-path])
         client-id (or (:client-id opts) (get-in env [:default :client-id]))
         user-id (or (:user-id opts) (get-in env [:default :user-id]))
         chart-id (:chart-id opts)]
     (println "charts-path: " charts-path " client-id: " client-id " user-id: " user-id " chart-id: " chart-id)
     (load-chart charts-path client-id user-id chart-id))))


(defn load-chart-boxed [charts-path client-id user-id chart-id]
  (let [data (load-chart charts-path client-id user-id chart-id)
        data-boxed (chart-box data)]
    (dissoc data-boxed :client :user)))

(defn delete-chart [charts-path client-id user-id chart-id]
  (info "deleting: " (filename-chart charts-path client-id user-id chart-id)))

;; explore

(defn dir? [filename]
  (-> (io/file filename) .isDirectory))

(defn split-filename [filename]
  (let [m (re-matches #"(.*)_(.*)_(.*)_(.*).edn" filename)
        [_ type client user chart] m]
    (when m
      {:type type
       :client-id client ;(Integer/parseInt client)
       :user-id user ;(Integer/parseInt user)
       :chart-id chart ;(Integer/parseInt chart)
       })))

(defn explore-dir [dir]
  (let [dir (io/file dir)
        files (if (.exists dir)
                (into [] (->> (.list dir)
                              (remove dir?)
                              (map split-filename)
                              (remove nil?)

                              doall))
                (do
                  (warnf "path not found: %s" dir)
                  []))]
    (debug "explore-dir: " files)
    ;(warn "type file:" (type (first files)) "dir?: " (dir? (first files)))
    files))

(defn user-files
  [type client-id user-id]
  (fn [i]
    (and (= type (:type i))
         (= client-id (:client-id i))
         (= user-id (:user-id i)))))

(defn timestamp-as-float [{:keys [timestamp] :as data}]
  (assoc data :timestamp (float timestamp)))

(defn chart-summary [charts-path {:keys [client-id user-id chart-id]}]
  (let [chart (load-chart charts-path client-id user-id chart-id)]
    (->   (select-keys chart [:name :symbol :resolution :id :timestamp])
          timestamp-as-float
          ;(rename-keys  {:chart :id})
          )))

(defn chart-list 
  ([charts-path client-id user-id]
   (info "chart list for: client: " client-id " user: " user-id)
   (->> (explore-dir charts-path)
        (filter (user-files "chart" client-id user-id))
        (map #(chart-summary charts-path %))
        (into [])))
  ([env]
   (let [charts-path (get-in env [:tradingview :charts-path])
         client-id  (get-in env [:default :client-id])
         user-id (get-in env [:default :user-id])]
     (chart-list charts-path client-id user-id)))
  ([env opts]
   (let [charts-path (get-in env [:tradingview :charts-path])
         client-id (or (:client-id opts) (get-in env [:default :client-id]))
         user-id (or (:user-id opts) (get-in env [:default :user-id]))]
     (chart-list charts-path client-id user-id))))

(comment

  (split-filename "chart_77_77_1636524198.edn")
  (split-filename ".placeholder")
  (explore-dir "./tv/chart")
  
;  
  )
