(ns study.pl
  (:require
   [tablecloth.api :as tc]
   [tech.v3.dataset :as ds]
   [tech.v3.datatype.functional :as dfn]
   [tech.v3.datatype.rolling :as rolling]))

(and 3 3 5 nil)

(defn exit-p [a b c d e]
  (if (and a b c d e)
    (cond
      (< b (* a 0.98)) (* a 0.98)
      (< c (* b 0.98)) (* b 0.98)
      (< d (* c 0.98)) (* c 0.98)
      (< e (* d 0.98)) (* d 0.98)
      :else e)
    0.0))

(defn exit-pl [a b c d e]
  (let [entry a
        exit (exit-p a b c d e)]
    (- (/ exit entry) 1.0)))

(defn add-exit-pl [ds]
  (tc/map-columns ds :pl [:px0 :px5 :px10 :px20 :px40] exit-pl))

(comment
  (exit-p 1 2 1.99 4 5)
  (exit-pl 1 2 1.99 4 5)

  (defn load-dataset [path]
    (-> (tc/dataset path {:key-fn keyword})
        (tc/order-by :date)))

  (-> (load-dataset "/home/florian/quantastore/study/signals.csv")
      (add-exit-pl)
      (tc/select-columns [:date :asset :close :pl])
      )
    ;:trailing-max-decline | :idx | :trailing-high-idx | :trailing-high-date | :setup |
  ;:signal | :initial |     :px0 |     :px5 |    :px10 |    :px20 |    :px40 |         :pl |
    
  ;
  
  )



