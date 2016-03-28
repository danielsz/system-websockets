(ns demo.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [taoensso.sente :as sente]
            [system.components.sente :refer [new-channel-socket-client]]
            [com.stuartsierra.component :as component]
            [ankha.core :as ankha]
            [cljs.core.match :refer-macros [match]]
            [ajax.core :refer [GET POST]]
            [cljs-utils.core :as utils :refer [by-id]]
            [om-flash-bootstrap.core :as f]
            [om-header-bootstrap.core :as h])
  (:require-macros 
   [cljs.core.async.macros :as a :refer [go go-loop]])
  (:import [goog.history Html5History]))

(def history (Html5History.))
(doto history
  (.setUseFragment false)
  (.setPathPrefix "") 
  (.setEnabled true))

(defn dev-mode? []
  (let [domain (.. js/window -location -hostname)]
    (= domain "localhost")))

(if (dev-mode?)
  (enable-console-print!)
  (set! *print-fn*
        (fn [& args]
          (do))))

(def app-state (atom {:view :root
                      :flash {}}))

(def flash #(om/ref-cursor (:flash (om/root-cursor app-state))))

;; sente

(def sente-client (component/start (new-channel-socket-client)))
(def chsk-send! (:chsk-send! sente-client))
(def chsk-state (:chsk-state sente-client))

(defn event-handler [event data owner]
  (.log js/console "Event: %s" (pr-str event))
  (match [event]
         [[:chsk/state state]] (do (.log js/console "state change: %s" (pr-str state))
                                   (if (= (:uid state) :taoensso.sente/nil-uid)
                                     (om/set-state! owner :session :unauthenticated)
                                     (om/set-state! owner :session :authenticated)))
         [[:chsk/handshake _]] (do (.log js/console "handshake"))
         [[:chsk/recv [:demo/flash payload]]] (do (.log js/console "Flash:" payload)
                                                        (f/info flash (:message payload)))
         [[:chsk/recv payload]] (.log js/console "Push event from server: %s" (pr-str payload))
         :else (.log js/console "Unmatched event: %s" event)))

(defn event-loop
  "Handle inbound events."
  [data owner]
  (go-loop [] 
    (let [{:as ev-msg :keys [event]} (<! (:ch-chsk sente-client))]
      (event-handler event data owner)
      (recur))))
;; sente

(defn login
  "Om component for new login"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "login")
    om/IRender
    (render [_]
      (dom/a #js {:href "/google/login"
                  :onClick (fn [e]
                             (let [url (.-href (.-target e))
                                   csrf-token (:csrf-token @chsk-state)]
                               (.preventDefault e)
                               (POST url
                                   {:headers {"X-CSRF-Token" csrf-token}
                                    :handler (fn [response] (set! (.-location js/window) (str response))) 
                                    :error-handler (fn [{:keys [status status-text]}]
                                                     (.log js/console (str "Error: " status " " status-text)))})))}
             "Sign in"))))

(defn header
  "Om component for new header"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "header")
    om/IRenderState
    (render-state [_ state]
      (let [left [["FAQ" #(.setToken history "/faq")]
                  ["Contact" #(set! (.. js/window -location -href) "mailto:daniel.szmulewicz@gmail.com?&subject=Demo")]]]
        (h/header data owner {:brand ["system demo" #(.setToken history "/")]
                              :left left
                              :authenticated [(dom/a #js {:href "/logout"} "Sign out")]
                              :unauthenticated [(om/build login data)]})))))

(defn ui
  "Om component for main ui"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "Main ui")
    om/IRender
    (render [_]
      (dom/div nil
                (dom/div #js {:className "panel panel-default"})))))

(declare not-found)
(defn app [data owner]
  (reify
    om/IInitState
    (init-state [this]
      {:session :unauthenticated})
    om/IDisplayName
    (display-name [this]
      "app")
    om/IWillMount
    (will-mount [this]
      (event-loop data owner))
    om/IRenderState
    (render-state [this state]
      (dom/div nil
               (om/build header data {:react-key "header" :state state})
               (om/build f/widget {:flash flash :timeout 2})
               (when (dev-mode?) (om/build ankha/inspector data))
        (condp = (:view data)
          :not-found (om/build not-found data)
          :root (case (:session state)
                  :authenticated (om/build ui data {:state state})
                  :unauthenticated (om/build ui data)))))))

(om/root app app-state
         {:target (by-id "main")
          :tx-listen (fn [{:keys [path old-value new-value old-state new-state tag] :as tx-data}
                         root-cursor]
                       (match [path]
                         :else (.log js/console (str "no match with cursor path " path))))})
