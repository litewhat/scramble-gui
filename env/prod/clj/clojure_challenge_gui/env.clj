(ns clojure-challenge-gui.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clojure-challenge-gui started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-challenge-gui has shut down successfully]=-"))
   :middleware identity})
