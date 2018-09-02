(ns clojure-challenge-gui.core
  (:require [baking-soda.core :as b]
            [reagent.core :as r]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [clojure-challenge-gui.ajax :as ajax]
            [ajax.core :refer [GET POST]]
            [secretary.core :as secretary :include-macros true])
  (:import goog.History))


(defonce session (r/atom {:page :scramble}))

(defn scramble-header []
  [:div.jumbotron
   [:h1 "Scramble"]])

(defn scramble-form []
  [:form
   [:div.form-group
    [:label {:for "available-letters-input"} "Available letters"]
    [:input {:id    "available-letters-input"
             :type  "text"
             :class "form-control form-control-lg"}]]

   [:div.form-group
    [:label {:for "searched-letters-input"} "Searched letters"]
    [:input {:id    "searched-letters-input"
             :type  "text"
             :class "form-control form-control-lg"}]]

   [:button {:type "button"
             :class "btn btn-success"}
    "Some action! "]])

(defn scramble-page []
  [:div.container
   [scramble-header]
   [scramble-form]])

(def pages
  {:scramble #'scramble-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/scramble" []
  (swap! session assoc :page :scramble))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
            (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------

(defn mount-components []
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
