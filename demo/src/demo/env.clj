(ns demo.env
  (:require
    [modular.system :refer [system]]
    [quanta.bar.protocol :as b]
    [quanta.bar.protocol :as b]
    [tick.core :as t]
    [missionary.core :as m]
   ))


(-> (:ss system)
    :barsource
    
    )

(:ss system)

(:bar-db-ss system)


(:bar-db-duck system)

(-> (:ctx system)
    
    
    )

(def bar-db (:barsource (:ss system)))
bar-db

 (m/? (b/get-bars bar-db {:asset "SPY" :calendar [:us :d]} 
                  {:start (t/instant "2005-01-01T00:00:00Z")
                   :end (t/instant "2010-03-01T20:00:00Z")}))

(m/? (b/get-bars bar-db {:asset "SPY" :calendar [:us :d]}
                 {:n 2
                  :end (t/instant "1967-02-10T00:00:00.000000000-00:00")
                  }))


                  
(m/? (b/get-bars bar-db {:asset "SPY" :calendar [:us :d]}
                 {:start (t/instant "1965-02-10T00:00:00.000000000-00:00")
                  :end (t/instant "1967-02-10T00:00:00.000000000-00:00")
                  }))


{:window {:n 329, :end #inst}, :opts {:asset "SSPY", :calendar [:us :d]} 

(def env
  {:tradingview  (:tradingview (system :config))
  
   ;:bar-db (system :bardb)
   :bar-db (:barsource (system :bardb-sa))
   :assetdb (system :assetdb)})