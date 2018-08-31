(ns clojure-challenge-gui.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [clojure-challenge-gui.core-test]))

(doo-tests 'clojure-challenge-gui.core-test)

