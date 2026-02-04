(ns quanta.tradingview.handler.config
  (:require
   [ring.util.response :as res]))

;; CONFIG - Tell TradingView which featurs are supported by server.

#_(def server-config
    {:supports_time true  ; we send our server-time
     :supports_search true ;search and individual symbol resolve logic.
     :supports_marks true
     :supports_timescale_marks false
     :supports_group_request false
     :supported_resolutions ["15" "D"] ; ["1" "5" "15" "30" "60" "1D" "1W" "1M"]
     :symbols_types [{:value "" :name "All"}
                     {:value "Crypto" :name "Crypto"}
                     {:value "Equity" :name "Equities"}
                     {:value "Mutualfund" :name "Mutualfund"}
                     {:value "ETF" :name "ETF"}]
     :exchanges [{:value "" :name "All Exchanges" :desc ""}
                 {:value "crypto" :name "Crypto" :desc ""}
                 {:value "us" :name "US Stocks" :desc ""}
               ;{:value "US" :name "US (Nasdaq NYSE)" :desc ""}
               ;{:value "GR" :name "German (Xetra/Regional)" :desc ""}
               ;{:value "NO" :name "Norway" :desc ""}
               ;{:value "AV" :name "Austria" :desc ""}
               ;{:value "LN" :name "London" :desc ""}
                 ]})

(defn config-handler [{:keys [ctx] :as _req}]
  (let [tradingview (:tradingview ctx)]
    (res/response (:config tradingview))))