(ns quanta.tradingview.events
  (:require
   [babashka.fs :as fs]
   [quanta.tradingview.persist :refer [spit-edn slurp-edn]]))

(defn list-events [{:keys [tradingview]}]
  (let [events-path (:events-path tradingview)]
    (->> (fs/list-dir events-path "*.edn")
         (map fs/file-name)
         (map fs/split-ext)
         (map first)
         (into []))))

(defn load-events [{:keys [tradingview]} event-name]
  (let [events-path (:events-path tradingview)]
    (slurp-edn (str events-path event-name ".edn"))))

(defn save-events [{:keys [tradingview]} event-name events]
  (let [events-path (:events-path tradingview)]
    (spit-edn (str events-path event-name ".edn") events)))


(comment
  (require '[modular.system :refer [system]])
  (def ctx (:ctx system))

  (list-events ctx)

  (->> (list-events ctx)
      (first)
      (load-events ctx))
   
  
  ;
  )