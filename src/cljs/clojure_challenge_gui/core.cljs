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


(defonce session (r/atom {:page :scramble
                          :scramble/available-letters "asdasdasdasd"
                          :scramble/searched-word "kupa"}))

(defn scramble-header []
  [:div.jumbotron
   [:h1 "Scramble"]])

(defn scramble-form []
  [:div.container
   [:form
    [:div.form-group
     [:label {:for "available-letters-input"} "Available letters"]
     [:input {:id        "available-letters-input"
              :type      "text"
              :class     "form-control form-control-lg"
              :value     (:scramble/available-letters @session)
              :on-change (fn [this]
                           (let [new-val (-> this .-target .-value)]
                             (swap! session
                                    assoc
                                    :scramble/available-letters
                                    new-val)))}]]

    [:div.form-group
     [:label {:for "searched-letters-input"} "Searched letters"]
     [:input {:id        "searched-letters-input"
              :type      "text"
              :class     "form-control form-control-lg"
              :value     (:scramble/searched-word @session)
              :on-change (fn [this]
                           (let [new-val (-> this .-target .-value)]
                             (swap! session
                                    assoc
                                    :scramble/searched-word
                                    new-val)))}]]

    [:button {:type     "button"
              :class    "btn btn-success"
              :on-click (fn []
                          (swap!
                            session
                            assoc
                            :scramble/result
                            "mocked-true"))}
     "Some action! "]]])

(defn scramble-result []
  [:div.container
   [:div.row
    [:div.col-sm-12
     (if-let [result (:scramble/result @session)]
       (str result))]]])

(defn scramble-page []
  [:div.container
   [scramble-header]
   [scramble-form]
   [scramble-result]])

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
