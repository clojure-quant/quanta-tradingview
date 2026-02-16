(ns study.app
  (:require
   [tablecloth.api :as tc]
   [tech.v3.dataset :as ds]
   [tech.v3.datatype.functional :as dfn]
   [tech.v3.datatype.rolling :as rolling]
   [indicator.decline :refer [add-trailing-decline add-trailing-decline-signal]]
   [data.bento :refer [ds ds2018 ds-liquid ds-liquid-2018]]))


(-> ds
    (tc/select-rows (fn [row]
                      (= (:asset row) "MSFT")))
    (add-trailing-decline 100))


(-> ds
    (tc/select-rows (fn [row]
                      (= (:asset row) "MSFT")))
    (add-trailing-decline-signal 100 -0.2 50)
    (tc/select-rows #(:signal %))
    ;(tc/select-rows #(:setup %))
    )

(defn select-signal [ds]
  (tc/select-rows ds #(:signal %)))

(defn compute-signals
  "Apply add-trailing-decline-signal to each asset group."
  [ds {:keys [window dd dd-n-min]}]
  (->> (tc/group-by ds :asset)
       (tc/groups->seq)
       ;(take 2)
       (pmap #(add-trailing-decline-signal (tc/as-regular-dataset %) window dd dd-n-min))
       (apply tc/concat)
       (select-signal)))


(-> ds-liquid
    (compute-signals {:window 300
                      :dd -0.2
                      :dd-n-min 120})
    (tc/write! "signals.csv"))




(-> ds-liquid-2018
    (compute-signals {:window 300
                      :dd -0.2
                      :dd-n-min 120})
    (tc/write! "signals2018.csv"))




   