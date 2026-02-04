(ns ta.tradingview.handler
  (:require
   [modular.webserver.middleware.api :refer [wrap-api-handler]]
   ; tradingview handlers..
   [ta.tradingview.handler.config :refer [config-handler]]
   [ta.tradingview.handler.asset :refer [search-handler symbols-handler]]
   ))


(def wrapped-config-handler (wrap-api-handler config-handler))
(def wrapped-symbols-handler (wrap-api-handler symbols-handler))
(def wrapped-search-handler (wrap-api-handler search-handler))



