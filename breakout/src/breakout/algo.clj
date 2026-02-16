(ns breakout.algo
  (:require
   [quanta2.algo :refer [register-algo]]
   [breakout.indicator.decline :refer [add-trailing-decline-signal]]))


(register-algo :breakout-up-from-decline
               (calculate [_ opts ds]
                          (println "running algo: " opts)
                          (println "running algo ds: " ds)
                          (add-trailing-decline-signal ds opts)
                          ;opts
                          ;ds
                          ))

