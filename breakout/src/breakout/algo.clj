(ns breakout.algo
   (:require
    [tablecloth.api :as tc]
    [quanta2.algo :refer [register-algo]]
    [breakout.indicator.decline :refer [add-trailing-decline-signal]]))


 (register-algo
  :breakout-up-from-decline
  (calculate [_ opts ds] 
             (add-trailing-decline-signal ds opts))
  (select-signal [_ opts ds] 
                 (tc/select-rows ds #(:signal %))))

