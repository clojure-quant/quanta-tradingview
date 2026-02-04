(ns quanta.tradingview.asset.db
  (:require
   [clojure.string :refer [includes? lower-case blank?]]
   [taoensso.timbre :refer [info]]
   [quanta.tradingview.asset.futures :refer [is-future? future-symbol]]))



(defn sanitize-name [{:keys [symbol] :as instrument}]
  (update instrument :name (fn [name] (if (or (nil? name)
                                              (blank? name))
                                        (str "Unknown: " symbol)
                                        name))))

(defn sanitize-category [instrument]
  (update instrument :category (fn [category] (if (nil? category)
                                                :equity
                                                category))))

(comment
  (sanitize-category {:symbol "a"})
  (sanitize-category {:symbol "a" :category :fx})
  (sanitize-category {:symbol "a" :category :crypto})
  ;
  )
(defn sanitize-exchange [{:keys [category] :as instrument}]
  (update instrument :exchange (fn [exchange] (if (nil? exchange)
                                                (if (= category :crypto)
                                                  :crypto
                                                  :us)
                                                exchange))))

(defn instrument-details [asset-db s]
  (if-let [f (is-future? s)]
    (let [data (get @asset-db (:symbol-root f))]
      (future-symbol f data))
    (get @asset-db s)))

(defn add [asset-db {:keys [asset] :as instrument}]
  (let [instrument (-> instrument
                       sanitize-name
                       sanitize-category
                       sanitize-exchange)]
    (swap! asset-db assoc asset instrument)))

(defn modify [asset-db {:keys [asset] :as instrument}]
  (let [old (instrument-details asset-db asset)
        merged (merge old instrument)]
    (swap! asset-db assoc asset merged)))

(defn get-instruments [asset-db]
  (-> @asset-db vals))

(defn get-symbols [asset-db]
  (->> @asset-db vals (map :asset)))

(defn symbols-available [asset-db category]
  (->> (get-instruments asset-db)
       (filter #(= category (:category %)))
       (map :asset)))

(defn q? [q]
  (fn [{:keys [name asset]}]
    (or (includes? (lower-case name) q)
        (includes? (lower-case asset) q))))

(defn =exchange? [e]
  (fn [{:keys [exchange]}]
    (= exchange e)))

(defn =category? [c]
  (fn [{:keys [category]}]
    (= category c)))

(defn filter-eventually [make-pred target list]
  (if target
    (filter (make-pred target) list)
    list))

(defn search
  ([asset-db q]
   (search asset-db q nil nil))
  ([asset-db q category]
   (search asset-db q category nil))
  ([asset-db q category exchange]
   (let [list-full (get-instruments asset-db)
         q (if (or (nil? q) (blank? q)) nil (lower-case q))
         e (if (nil? exchange)  nil exchange)
         c (if (nil? category) nil category)]
     (info "search q: " q "category: " c " exchange: " e)
     (->> list-full
          (filter-eventually =exchange? e)
          (filter-eventually =category? c)
          (filter-eventually q? q)))))

(defn instrument-name [asset-db asset]
  (-> asset #(instrument-details asset-db %) :name))

(defn get-instrument-by-provider [asset-db provider s]
  (some (fn [instrument]
          (let [ps (provider instrument)]
            (when (= ps s)
              instrument)))
        (vals @asset-db)))



(defn start-asset-db [assets]
  (let [asset-db (atom {})]
    (doall (map #(add asset-db %) assets))
    asset-db))

;(defonce db (atom {}))