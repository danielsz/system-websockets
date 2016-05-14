(ns demo.systems
  (:require
   [demo.handler :refer [ring-handler site sente-handler]]
   [demo.middleware
    [not-found :refer [wrap-not-found]]]
   [com.stuartsierra.component :as component]
   [ring.middleware.defaults :refer [wrap-defaults]]
   [taoensso.sente.server-adapters.immutant :refer (sente-web-server-adapter)]
   (system.components
    [immutant-web :refer [new-web-server]]
    [sente :refer [new-channel-sockets sente-routes]]
    [h2 :refer [new-h2-database DEFAULT-MEM-SPEC DEFAULT-DB-SPEC]]
    [repl-server :refer [new-repl-server]]
    [endpoint :refer [new-endpoint]]
    [handler :refer [new-handler]]
    [middleware :refer [new-middleware]])
   [environ.core :refer [env]]))

(defn dev-system
  "Assembles and returns components for a base application"
  []
  (component/system-map
   :db (new-h2-database DEFAULT-MEM-SPEC)
   :sente (component/using
           (new-channel-sockets sente-handler sente-web-server-adapter {:wrap-component? true})
           [:db])
    :sente-endpoint (component/using
                     (new-endpoint sente-routes)
                     [:sente])
    :routes (component/using
            (new-endpoint ring-handler)
            [:db])
    :middleware (new-middleware {:middleware [[wrap-defaults :defaults]
                                              [wrap-not-found :not-found]]
                                 :defaults site
                                 :not-found  "<h2>The requested page does not exist.</h2>"})
    :handler (component/using
             (new-handler)
             [:sente-endpoint :routes :middleware])
    :http (component/using
          (new-web-server (Integer. (env :http-port)))
          [:handler])))

