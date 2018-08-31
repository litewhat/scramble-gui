(ns clojure-challenge-gui.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [clojure-challenge-gui.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[clojure-challenge-gui started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-challenge-gui has shut down successfully]=-"))
   :middleware wrap-dev})
