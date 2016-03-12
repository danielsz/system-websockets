(ns demo.html
  (:require 
   (hiccup [core :refer [html]]
           [page :refer [html5 include-js include-css]])))

(defn index
  "Main skeleton for the application"
  []
  (html5
   [:head 
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:meta {:name "description" :content "Demo"}]
    [:meta {:name "author" :content "Daniel Szmulewicz"}]
    [:title "System client demo"]

    (include-css 
      "/css/main.css"
      "//maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css"
     "//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
     "//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css")

    "<!--[if lt IE 9]>"
    [:script {:src "https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"}]
    [:script {:src "https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"}]
    "<![endif]-->"
    ]
   [:body     
    [:div.container-fluid {:id "main"}]
    [:script {:src "main.js" :type "text/javascript"}]
    (include-js
     "https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"
      "//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js")]))
