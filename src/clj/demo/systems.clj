(ns demo.systems
  (:require
   [demo.handler :refer [ring-handler sente-handler]]
   [demo.middleware
    [not-found :refer [wrap-not-found]]]
   [com.stuartsierra.component :as component]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]
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

(def site
  (-> site-defaults
      (assoc-in [:static :resources] "/")))

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
    :app-middleware (new-middleware {:middleware [wrap-restful-format]})
    :app-endpoints (component/using
            (new-endpoint ring-handler)
            [:db :app-middleware])
    :middleware (new-middleware {:middleware [[wrap-defaults :defaults]
                                              [wrap-not-found :not-found]]
                                 :defaults site
                                 :not-found  "<h2>The requested page does not exist.</h2>"})
    :handler (component/using
             (new-handler)
             [:sente-endpoint :app-endpoints :middleware])
    :http (component/using
          (new-web-server (Integer. (env :http-port)))
          [:handler])))

