(ns clojure-challenge-gui.routes.home
  (:require [clojure-challenge-gui.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defn home-page []
  (-> (layout/render "home.html")
      (response/header "Access-Control-Allow-Headers" "Content-Type")))

(defroutes home-routes
  (GET "/" []
    (home-page))

  (GET "/docs" []
    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
        (response/header "Content-Type" "text/plain; charset=utf-8"))))
