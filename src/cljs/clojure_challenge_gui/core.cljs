(ns clojure-challenge-gui.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [clojure-challenge-gui.ajax :as ajax]
            [ajax.core :refer [GET POST]]
            [secretary.core :as secretary :include-macros true]
            [taoensso.timbre :as logger])
  (:import goog.History))


;;;;;;;;;;;;;
;; SESSION ;;
;;;;;;;;;;;;;

(defonce session (r/atom {:page                       :scramble
                          :scramble/available-letters "iseeyou:)"
                          :scramble/searched-word     "me?"
                          :scramble/result           nil
                          :scramble/response          nil}))

(logger/debug "scramble repsonse:" (:scramble/response @session))


;;;;;;;;;;;;;;;;;;;
;; AJAX HANDLERS ;;
;;;;;;;;;;;;;;;;;;;

(defn ajax-handler [{:keys [body headers status] :as resp}]
  (swap! session assoc
         :scramble/response resp
         :scramble/result   (:result resp))
  (logger/debug "scramble response:" (:scramble/response @session))
  (logger/debug "scramble result:"   (:scramble/result @session)))

(defn ajax-error-handler [{:keys [body headers status status-text] :as resp}]
  (swap! session (fn [sess] (assoc sess :scramble/response resp)))
  (logger/debug (:scramble/response @session)))


(defn make-call-to-api []
  (go
    (POST "http://localhost:8000/api/scramble"
          {:params          {:str1 (:scramble/available-letters @session)
                             :str2 (:scramble/searched-word @session)}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         ajax-handler
           :error-handler   ajax-error-handler})))


;;;;;;;;;;;;;
;; HELPERS ;;
;;;;;;;;;;;;;

(defn value [x] (-> x .-target .-value))


;;;;;;;;;;;;;;;;
;; COMPONENTS ;;
;;;;;;;;;;;;;;;;

(defn available-letters-form-input []
  (let [initial-value (:scramble/available-letters @session)
        on-change-fn  (fn [this]
                        (let [new-val (-> this .-target .-value)]
                          (swap! session assoc
                            :scramble/available-letters new-val
                            :scramble/result nil)))]
    [:div.form-group
     [:label {:for "available-letters-input"} "Available letters"]
     [:input {:id        "available-letters-input"
              :type      "text"
              :class     "form-control form-control-lg"
              :value     initial-value
              :on-change on-change-fn}]]))

(defn searched-word-form-input []
  (let [initial-value (get @session :scramble/searched-word)
        on-change-fn  (fn [field]
                        (swap! session assoc
                               :scramble/searched-word (value field)
                               :scramble/result nil))]
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
   {:type     "button"
    :class    "btn btn-success"
    :on-click make-call-to-api}
   "Click here!"])


;;;;;;;;;;;;;;;
;;
;;;;;;;;;;;;;

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
     (let [result (:scramble/result @session)]
       (if (or (= result false) (= result true))
         (str result)
         (str "")))]]])

(defn scramble-page []
  [:div.container
   [scramble-header]
   [scramble-form]
   [scramble-result]])


;;;;;;;;;;;
;; PAGES ;;
;;;;;;;;;;;

(def pages
  {:scramble #'scramble-page})

(defn page []
  [(get pages (:page @session))]
  [(pages (:page @session))])


;;;;;;;;;;;;
;; ROUTES ;;
;;;;;;;;;;;;

(secretary/set-config! :prefix "#")

(secretary/defroute "/scramble" []
  (swap! session assoc :page :scramble))


;;;;;;;;;;;
;; HOOKS ;;
;;;;;;;;;;;

(defn hook-browser-navigation!
  "Must be called after routes have been defined"
  []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))


(defn mount-components []
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
