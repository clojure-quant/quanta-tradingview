(ns quanta.tradingview.handler.response.asset
  (:require
   [clojure.string :as str]
   [quanta.market.asset.datahike :refer [query-assets get-asset
                                         provider->asset asset->provider
                                         exchanges categories]]))

(defn instrument->tradingview [a]
  {:ticker (:asset/symbol a)
   :name (:asset/symbol a)
   :symbol (:asset/symbol a) ; OUR SYMBOL FORMAT. TV uses exchange:symbol
   :full_name (:asset/name a)
   :description  (:asset/name a)
   :exchange (:asset/exchange a)
   :exchange-listed (:asset/exchange a)
   :type (name (:asset/category a))})


(def example-symbolinfo-response-error
  {"s" "error"
   "errmsg" "unknown_symbol FX:EURUSD"})

(def example-symbolinfo-response-success
{"name" "MSFT"
 "exchange-traded" "NasdaqNM"
 "exchange-listed" "NasdaqNM"
 "timezone" "America/New_York"
 "minmov" 1
 "minmov2" 0
 "pointvalue" 1
 "session" "0930-1630"
 "has_intraday" false
 "visible_plots_set" "ohlcv"
 "description" "Microsoft Corporation"
 "type" "stock"
 "supported_resolutions" ["D" "2D" "3D" "W" "3W" "M" "6M"]
 "pricescale" 100
 "ticker" "MSFT"
 "logo_urls" ["https://s3-symbol-logo.tradingview.com/microsoft.svg"]
 "exchange_logo" "https://s3-symbol-logo.tradingview.com/country/US.svg"})

(defn symbol-info
  "Converts instrument [from db] to tradingview symbol-information
   Used in symbol and search"
  [assetdb s]
  (if-let [i (get-asset assetdb s)]
    (merge (instrument->tradingview i)
           {:supported_resolutions [;"15"
                                    "D"]

                ; FORMATTING OF DIGITS
            :minmov 1  ; is the amount of price precision steps for 1 tick. For example, since the tick size for U.S. equities is 0.01, minmov is 1. But the price of the E-mini S&P futures contract moves upward or downward by 0.25 increments, so the minmov is 25.
            :pricescale 100 ;  If a price is displayed as 1.01, pricescale is 100; If it is displayed as 1.005, pricescale is 1000.
            :minmov2 0  ;  for common prices is 0 or it can be skipped.
            :fractional 0  ; for common prices is false or it can be skipped.   ; Fractional prices are displayed 2 different forms: 1) xx'yy (for example, 133'21) 2) xx'yy'zz (for example, 133'21'5).
            :volume_precision 0 ;Integer showing typical volume value decimal places for a particular symbol. 0 means volume is always an integer. 1 means that there might be 1 numeric character after the comma.
                ; :has_no_volume false
            :pointvalue 1
                ; session
            :data_status "endofday"  ; streaming endofday pulsed delayed_streaming delayed  delayed_streaming 
            :has_intraday true
            :timezone "Etc/UTC" ; "America/New_York"
            :session "0900-1600"  ;"0900-1630|0900-1400:2",
                                      ;:session-regular "0900-1600"
            :has_empty_bars true

                ; :expired true ; whether this symbol is an expired futures contract or not.
                ; :expiration_date  (to-epoch-no-ms- (-> 1 t/hours t/ago))
            })
    {:s "error"
     :errmsg (str "unknown_symbol " s)}))


(defn symbol-search [assetdb query type exchange limit]
  (let [category (when (and type (string? type) (not (str/blank? type)))
                   [:category (keyword type)])
        exchange (when (and exchange (string? exchange) (not (str/blank? exchange)))
                   [:exchange exchange])
        q (when (and query (string? query) (not (str/blank? query)))
            [:q query])
        qp (into {} [category exchange q])
        result (query-assets assetdb qp)
        result (take limit result)
        result-tv (map instrument->tradingview result)]
    result-tv))

(comment
  (symbol-search "B" "" "" 10)
  (symbol-search "" "crypto" "" 10)
  (symbol-search "" "" "crypto" 10)
  (keyword "crypto")
  (name :crypto)
 ;
  )



