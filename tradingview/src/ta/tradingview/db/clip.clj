(ns ta.tradingview.db.clip
  (:require
   [modular.system]))



(def charts-path "../../output/tradingview-charts/")

(def template-path "../resources/tradingview-templates/")

(def marks-path "../../data/")

(defn import-for-exchange [exchange]
  (cond
    (= exchange :crypto) {:import :bybit}
    :else {:import :kibot}))
