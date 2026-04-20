(ns quanta.tradingview.persist
  (:require
   [ednx.tick.edn :refer [add-tick-edn-handlers!]]
   [ednx.tick.fipp :refer [add-tick-fipp-printers!]]
   [ednx.fipp :as f]
   [ednx.edn :as e]))
   

(add-tick-edn-handlers!)
(add-tick-fipp-printers!)



(def spit-edn f/spit-fipp)

(def slurp-edn e/slurp-edn)
