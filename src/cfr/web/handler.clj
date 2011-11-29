(ns cfr.web.handler
  (:use     [compojure.core])
  (:require [compojure.route        :as route]
            [compojure.handler      :as handler]
            [cfr.model.core         :as model]
            [cfr.model.game-streaks :as gs]
            [cfr.web.layout         :as layout]
            [cfr.web.html           :as h]))

(defn- paged [q uri params o l body-fn]
  (let [lim (Integer. (or l 20)) off (Integer. (or o 0))]
    (h/simple-pager
      [uri params]
      (model/pager q off lim)
      (body-fn (-> q (model/for-list off lim) (model/run))))))

(defn all-time-win-streaks [o l uri params]
  (paged (-> gs/query (gs/win)) uri params o l h/win-streaks))

(defroutes main-routes
  (GET "/all-time-win-streaks" [o l :as {uri :uri, params :params}] 
    (all-time-win-streaks o l uri params)))

(def app
  (-> main-routes
    (handler/site)
    (layout/wrap-layout)))

;;(def app (handler/site main-routes))
