(ns study.test
   (:require
    [tick.core :as t]
    [tablecloth.api :as tc]
    [missionary.core :as m]
    [quanta.bar.protocol :as b]
    [quanta2.barstudy :refer [run bar-all->map]]
    [quanta2.algo :refer [available-algos]]
    [demo.env :refer [env]]))

 (m/? (b/summary (:bar-db env) {:calendar [:us :d]}))

 (m/? (b/get-bars (:bar-db env)  {:asset "A" :calendar [:us :d]} {}))

 (available-algos)


 (-> (m/? (run env {:algo :breakout-up-from-decline
                    :algo-opts {:window-n 300
                                :dd-max -0.2
                                :dd-min-n 120}
                    :asset ["A"]
                    :list "flo"
                    :calendar [:us :d]
                    :window {:start (t/instant "2020-02-10T00:00:00Z")}}))
     ;(get "SPY")
  )
 
 



(m/? (run env {:asset ["ORCL"] :list "flo"}))
