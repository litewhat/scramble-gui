(ns clojure-challenge-gui.app
  (:require [clojure-challenge-gui.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
