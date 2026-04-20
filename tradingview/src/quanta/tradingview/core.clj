(ns quanta.tradingview.core
  (:require
   [modular.env :refer [env]]))


(defn setup-tradingview [config]
  (-> config
      (update :charts-path env)
      (update :template-path env)
      (update :marks-path env)
      (update :events-path env)))

