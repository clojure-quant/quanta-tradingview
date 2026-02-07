(ns quanta.tradingview.chart.db
  (:require
   [taoensso.timbre :refer [debug info warnf]]
   [tick.core :as tick]
   [cljc.java-time.instant :as ti]
   [clojure.java.io :as io]
   [babashka.fs :refer [create-dirs]]
   [modular.persist.edn] ; side effects to load edn files
   [modular.persist.protocol :refer [save loadr]]))

(defn now-epoch []
  (-> (tick/now)
      (ti/get-epoch-second)))

;; save / load / delete

(defn filename-chart  [charts-path client-id user-id chart-id]
  (str charts-path "chart_" client-id "_" user-id "_" chart-id ".edn"))

(defn save-chart
  ([charts-path client-id user-id chart-id data]
   (let [data (assoc data
                     :timestamp (now-epoch)
                     :id chart-id ;(if (string? chart-id)  (Integer/parseInt chart-id) chart-id)
                     :client client-id ; (if (string? client-id)  (Integer/parseInt client-id) client-id)
                     :user user-id ;(if (string? user-id)  (Integer/parseInt user-id) user-id)
                     )]
     (create-dirs charts-path)
     (save :edn (filename-chart charts-path client-id user-id chart-id) data)
     (info "saved chart id: " chart-id)))
  ([env opts data]
   (let [charts-path (get-in env [:tradingview :charts-path])
         client-id (or (:client-id opts) (get-in env [:default :client-id]))
         user-id (or (:user-id opts) (get-in env [:default :user-id]))
         chart-id (or (:chart-id opts) (:chart-id data))]
     (println "charts-path: " charts-path " client-id: " client-id " user-id: " user-id " chart-id: " chart-id)
     (save-chart charts-path client-id user-id chart-id data))))

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