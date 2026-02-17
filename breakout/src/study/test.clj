(ns study.test
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [quanta.bar.protocol :as b]
   [quanta2.barstudy :refer [run bar-all->map]]
   [quanta2.algo :refer [available-algos]]
   [quanta2.asset-list :refer [show-lists]]
   [breakout.algo] ; for side effects
   [study.pl :refer [add-exit-pl]]
   [demo.env :refer [env]]))

(m/? (b/summary (:bar-db env) {:calendar [:us :d]}))

(m/? (b/get-bars (:bar-db env)  {:asset "A" :calendar [:us :d]} {}))
(m/? (b/get-bars (:bar-db env)  {:asset "QQQ" :calendar [:us :d]} {}))

(available-algos)

(show-lists env)
; ("flo" "equity-10mio" "equity-20mio" "etf-10mio" "equity-100mio")

(time
 (-> (m/? (run env {:algo :breakout-up-from-decline
                    :algo-opts {:window-n 300
                                :dd-max -0.2
                                :dd-min-n 120}
                    :asset ["A"]
                    :list "flo"
                    :calendar [:us :d]
                    :window {:start (t/instant "1990-02-10T00:00:00Z")}}))
     :signals
     (add-exit-pl)
     (tc/select-columns [:date :asset :close :pl])
     ;(tc/write! "/home/florian/quantastore/study/signals.csv")
     ))



(def demo
  (time
   (-> (m/? (run env {:algo :breakout-up-from-decline
                      :algo-opts {:window-n 300
                                  :dd-max -0.2
                                  :dd-min-n 120}
                      :asset ["A"]
                      :list "etf-10mio"
                      :calendar [:us :d]
                      :window {:start (t/instant "1990-02-10T00:00:00Z")}}))
     ;(tc/select-columns [:date :asset :close :pl])
       )))

; running on assets:  951
; "Elapsed time: 20151.718038 msecs"

(:errors demo)


(->> ds-etf
     :signals
    ;(add-exit-pl) 
 )

(-> demo 
    ()
    )


(-> demo
    :signals
    (tc/select-rows (fn [row]
                      (> (:close row) 1.0)))
    (tc/select-columns [;:date 
                        :asset :close :pl :px0 :px5 :px10 :px20 :px40])

    (add-exit-pl)
    ;(tc/info)
    )

(:errors demo)
;("MUSE" "BCHP" "GRNJ" "CVRT" "BSR" "QTOP" "WTIU")