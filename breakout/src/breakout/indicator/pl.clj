(ns breakout.indicator.pl
  (:require
   [tablecloth.api :as tc]
   [tech.v3.dataset :as ds]
   [tech.v3.datatype.functional :as dfn]
   [tech.v3.datatype.rolling :as rolling]))

(and 3 3 5 nil)

(defn exit-p [p0 p5 p10 p20 p40]
  (if (and p0 p5 p10 p20 p40)
    (cond
      (< p5 (* p0 0.98)) (* p0 0.98)
      (< p10 (* p5 0.98)) (* p5 0.98)
      (< p20 (* p10 0.98)) (* p10 0.98)
      (< p40 (* p20 0.98)) (* p20 0.98)
      :else p40)
    0.0))

(defn exit-pl [a b c d e]
  (let [entry a
        exit (exit-p a b c d e)]
    (- (/ exit entry) 1.0)))

(defn add-exit-pl [ds]
  (tc/add-column ds :pl
                 (->> (map exit-pl
                           (:px0 ds) (:px5 ds) (:px10 ds)
                           (:px20 ds) (:px40 ds))
                      (into []))))


  (comment
    (exit-p 1 2 1.99 4 5)
    (exit-pl 1 2 1.99 4 5)

    (defn load-dataset [path]
      (-> (tc/dataset path {:key-fn keyword})
          (tc/order-by :date)))

    (-> (load-dataset "/home/florian/quantastore/study/signals.csv")
        (add-exit-pl)
        (tc/select-columns [:date :asset :close :pl]))
    ;:trailing-max-decline | :idx | :trailing-high-idx | :trailing-high-date | :setup |
  ;:signal | :initial |     :px0 |     :px5 |    :px10 |    :px20 |    :px40 |         :pl |

  ;
    )



