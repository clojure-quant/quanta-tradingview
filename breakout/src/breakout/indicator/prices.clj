(ns breakout.indicator.prices
  (:require
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.functional :as dfn]
   [tablecloth.api :as tc]))

(defn future-col [col offset]
  (let [n (count col)
        max-idx (- n offset)]
    (dtype/make-reader :float64 n
                       (if (< idx max-idx)
                         (col (+ idx offset))
                         (col idx)))))

(defn add-future-prices
  "Adds future close prices to a dataset of bars using tablecloth.
   - :px0: next bar's close
   - :px5: close price 5 bars in the future
   - :px10: close price 10 bars in the future
   - :px20: close price 20 bars in the future
   - :px40: close price 40 bars in the future"
  [bars]
  (let [close (:close bars)]
    (-> (tc/add-columns
         bars
         {:px0 (future-col close 1)
          :px5 (future-col close 5)
          :px10 (future-col close 10)
          :px20 (future-col close 20)
          :px40 (future-col close 40)}))))



  (comment
    (def d (tc/dataset {:a [1.0 2.0 3.0 4.0 5.0]
                        :b [1.0 2.0 3.0 4.0 5.0]
                        :c [1.0 2.0 3.0 4.0 100.0]}))

    (-> d
        :a
        (future-col 2)))