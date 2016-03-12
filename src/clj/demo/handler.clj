(ns demo.handler
  (:require
   [demo.html :as html]
   [demo.helpers.session :as helper]
   [compojure.core :refer [routes GET POST ANY]]
   [compojure.route :as route]
   [ring.middleware.defaults :refer [site-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [demo.authentication.github :as github]
   [clojure.core.match :as match :refer (match)]
   [environ.core :refer [env]]))

(defn ring-handler [{db :db}]
  (-> (routes
       (GET "/" [] (html/index))
       (GET "/status" req (helper/status req))
       (GET "/logout" req (helper/logout req)))
      wrap-restful-format))

(def site
  (-> site-defaults
      (assoc-in [:static :resources] "/")))

(defn sente-handler [{{db-spec :db-spec} :db}] ""
  (fn [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)
          headers (:headers ring-req)
          uid     (:uid session)
          [id data :as ev] event]

      (println "Event:" session)
      (match [id data]
             :else nil))))
