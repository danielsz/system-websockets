(ns demo.helpers.session
  (:require [ring.util.response :as util :refer [response redirect content-type]]))

(defn logged-in? [req]
  (if (:uid (:session req)) true false))

(defn status [{session :session headers :headers cookies :cookies :as req}]
  (-> (str "Session: " session 
        "\nCookies: " cookies 
        "\nHeaders: " headers 
        "\nLogged in: " (logged-in? req))
    (response)
    (content-type "txt")))

(defn logout [{session :session}]
  (let [session {}]
    (-> (redirect "/")
      (assoc :flash {:message "You have been logged out." :level :success})
      (assoc :session session))))
