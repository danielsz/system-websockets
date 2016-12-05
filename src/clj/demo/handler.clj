(ns demo.handler
  (:require
   [demo.html :as html]
   [demo.helpers.session :as helper]
   [compojure.core :refer [routes GET POST ANY]]
   [compojure.route :as route]
   [ring.util.response :as util]
   [clojure.core.match :as match :refer (match)]
   [environ.core :refer [env]]))

(defn ring-handler [{db :db}]
  (routes
   (GET "/" [] (html/index))
   (POST "/signin" req (let [session (assoc (:session req) :uid "John Doe")]
                         (-> (util/response "John Doe")
                             (assoc :session session))))
   (GET "/status" req (helper/status req))
   (GET "/logout" req (helper/logout req))))

(defn sente-handler [{db :db}]
  (fn [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)
          headers (:headers ring-req)
          uid     (:uid session)
          [id data :as ev] event]

      (println "Session:" session)
      (match [id data]
             :else nil))))
