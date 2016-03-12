(ns demo.middleware.autologin)

(defn autologin [app]
  (fn [{params :params session :session headers :headers :as request}]
    (binding [*out* *err*]
      (println params))
    (if-let [uid (:uid session)]
      (app request)
      (if-let [me (:me session)]
          (let [session (-> session
                            (assoc :uid (:email me)))]
            (app (assoc request :session session)))
          (app request)))))
