(ns clojure-challenge-gui.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [clojure-challenge-gui.ajax :as ajax]
            [ajax.core :refer [GET POST]]
            [secretary.core :as secretary :include-macros true])
  (:import goog.History))


(defonce scramble-response (r/atom {}))


(defonce session (r/atom {:page                       :scramble
                          :scramble/available-letters "iseeyou:)"
                          :scramble/searched-word     "me?"
                          :scramble/result            nil}))

(defn ajax-handler [{:keys [body headers status] :as resp}]
  (reset! scramble-response resp)
  (swap! session assoc :scramble/result resp)
  (js/console.log (str "Received response"
                       " | Response: " resp
                       " | Headers: " headers
                       " | Status: " status
                       " | Body: " body)))

(defn ajax-error-handler [{:keys [body headers status status-text] :as resp}]
  (reset! scramble-response resp)
  (js/console.log
    (str "Something bad happened!"
         " | Response: " resp
         " | Headers: " headers
         " | Status: " status
         " | Status-text: " status-text
         " | Body: " body)))


(defn make-example-ajax-call []
  (go
    (POST "http://localhost:8000/api/scramble"
          {:params          {:str1 (:scramble/available-letters @session)
                             :str2 (:scramble/searched-word @session)}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         ajax-handler
           :error-handler   ajax-error-handler})))


(defn available-letters-form-input []
  (let [initial-value (:scramble/available-letters @session)
        on-change-fn  (fn [this]
                        (let [new-val (-> this .-target .-value)]
                          (swap!
                            session
                            assoc
                            :scramble/available-letters
                            new-val)))]
    [:div.form-group
     [:label {:for "available-letters-input"} "Available letters"]
     [:input {:id        "available-letters-input"
              :type      "text"
              :class     "form-control form-control-lg"
              :value     initial-value
              :on-change on-change-fn}]]))

(defn value [x] (-> x .-target .-value))

(defn searched-word-form-input []
  (let [initial-value (get @session :scramble/searched-word)
        on-change-fn  (fn [field]
                        (swap! session assoc :scramble/searched-word (value field)))]
    [:div.form-group
     [:label {:for "searched-letters-input"}
      "Searched letters"]
     [:input {:id        "searched-letters-input"
              :type      "text"
              :class     "form-control form-control-lg"
              :value     initial-value
              :on-change on-change-fn}]]))

(defn send-scramble-form-button []
  [:button
   {:type "button" :class "btn btn-success" :on-click make-example-ajax-call}
   "Some action! "])


(defn scramble-header []
  [:div.jumbotron
   [:h1 "Scramble"]])

(defn scramble-form []
  [:div.container
   [:form
    [available-letters-form-input]
    [searched-word-form-input]
    [send-scramble-form-button]]])

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
