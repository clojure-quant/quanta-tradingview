(ns study.duck
   (:require
    [tick.core :as t]
    [tablecloth.api :as tc]
    [babashka.fs :as fs]
    [clojure.string :as str]
    [missionary.core :as m]
    [quanta.calendar.window :refer [date-range->window]]
    [quanta.bar.protocol :as b]
    [quanta.bar.db.duck :as duck]
    [quanta.bar.db.duck.warehouse :as wh]
    [longus.bento-loader :refer [ds ds2018 ds-liquid ds-liquid-2018]]))


 (fs/delete-tree "./duck")
 (when-not (fs/exists? "./duck")
   (fs/create-dir "./duck"))



 (def db-duck (duck/start-bardb-duck "./duck/bento.ddb"))

 (duck/stop-bardb-duck db-duck)

 (tc/info ds2018)

 (defn string-date-to-instant [s]
   (t/instant (str (subs s 0 10) "T00:00:00+00:00")))

 ;(string-date-to-instant "2026-01-29 00:00:00+00:00")

 (defn- ensure-date-instant
   "duckdb needs one fixed type for the :date column.
   we use instant, which techml calls packed-instant"
   [ds]
   (tc/add-column ds :date (map string-date-to-instant (:date ds))))


 (def ds-all-ok
   (-> ds2018
       (tc/select-rows (fn [row]
                         (and (:asset row)
                              (not (= (:asset row) "XXX")))))
       ensure-date-instant))

 (tc/info ds-all-ok)
 ds-all-ok


 (-> ds-all-ok
     (tc/select-rows (fn [row]
                       (= (:asset row) "IBM"))))

 (defn append-asset [{:keys [asset bars ds]}]
   (println "appending " asset " with " bars " bars ..")
   (m/? (b/append-bars db-duck {:asset asset
                                :calendar [:us :d]} ds)))



 (->> (tc/group-by ds-all-ok :asset)
      (tc/groups->map)
      (map (fn [[asset ds]]
             {:asset asset
              :bars (tc/row-count ds)
              :ds ds}))
      (filter (fn [{:keys [asset bars]}]
                (> bars 700)))
      (map append-asset)
      (map :asset)
      (doall))
; 4800

; 2018-2019-2020-2021-2022-2023-2024-2025
; 8 years * 200 = 1600



 (def window (date-range->window
              [:us :d]
              {:start (-> "1999-02-01T20:00:00Z" t/instant)
               :end (-> "2026-03-01T20:00:00Z" t/instant)}))
 window

; just get the window

 (m/? (b/get-bars db-duck
                  {:asset "IBM"
                   :calendar [:us :d]}
                  window))


 (wh/warehouse-summary db-duck [:us :d])

#warehouse [:us :d] [2417 4]:
#| :asset |               :start |                 :end | :count |
#|--------|----------------------|----------------------|-------:|
#|      A | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1884 |
#|     AA | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    AAC | 2018-05-01T00:00:00Z | 2023-11-06T00:00:00Z |   1036 |
#|    AAN | 2018-05-01T00:00:00Z | 2024-10-03T00:00:00Z |   1618 |
#|    AAP | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    AAT | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|     AB | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    ABB | 2018-05-01T00:00:00Z | 2023-05-22T00:00:00Z |   1274 |
#|   ABBV | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    ABC | 2018-05-01T00:00:00Z | 2023-08-29T00:00:00Z |   1342 |
#|    ... |                  ... |                  ... |    ... |
#|    YRD | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    YUM | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1882 |
#|   YUMC | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    ZBH | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    ZEN | 2018-05-01T00:00:00Z | 2022-11-21T00:00:00Z |   1150 |
#|    ZNH | 2018-05-01T00:00:00Z | 2023-02-02T00:00:00Z |   1199 |
#|    ZTO | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    ZTR | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    ZTS | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1948 |
#|    ZUO | 2018-05-01T00:00:00Z | 2025-02-13T00:00:00Z |   1708 |
#|   ZYME | 2018-05-01T00:00:00Z | 2026-01-29T00:00:00Z |   1878 |