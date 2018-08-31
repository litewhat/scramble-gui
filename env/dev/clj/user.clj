(ns user
  (:require [clojure-challenge-gui.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [clojure-challenge-gui.figwheel :refer [start-fw stop-fw cljs]]
            [clojure-challenge-gui.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'clojure-challenge-gui.core/repl-server))

(defn stop []
  (mount/stop-except #'clojure-challenge-gui.core/repl-server))

(defn restart []
  (stop)
  (start))


