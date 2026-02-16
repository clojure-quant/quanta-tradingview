(ns study.2018
  (:require
   [tablecloth.api :as tc]
   [tech.v3.dataset :as ds]
   [tech.v3.datatype.functional :as dfn]
   [tech.v3.datatype.rolling :as rolling]))

(defn load-dataset [path]
  (-> (tc/dataset path {:key-fn keyword})
      (tc/order-by [:asset :date])
      (tc/add-column :turnover #(dfn/* (:close %) (:volume %)))))

(defn exit-p [a b c d e]
  (cond
    (< b (* a 0.98)) (* a 0.98)
    (< c (* b 0.98)) (* b 0.98)
    (< d (* c 0.98)) (* c 0.98)
    (< e (* d 0.98)) (* d 0.98)
    :else e))

(defn exit-pl [a b c d e]
  (let [entry a
        exit (exit-p a b c d e)]
    (- (/ exit entry) 1.0)))

(exit-p 1 2 1.99 4 5)
(exit-pl 1 2 1.99 4 5)

(load-dataset "signals2018.csv")


(-> (load-dataset "signals2018.csv")
    (tc/map-columns :pl [:px0 :px5 :px10 :px20 :px40] exit-pl)
    (tc/write! "2018-pl2.csv")
    )

