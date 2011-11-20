(ns cfr.web.core
  (:use [compojure.core]
        [hiccup.core]
        [ring.util.codec :only [url-encode]])
  (:require [compojure.route        :as route]
            [compojure.handler      :as handler]
            [cfr.model.core :as model]
            [cfr.model.game-streaks :as gs]))

(defn make-query-string [m]
  (let [s #(if (instance? clojure.lang.Named %) (name %) %)]
    (->> (for [[k v] m]
           (str (url-encode (s k) "UTF-8")
           "="
           (url-encode (str v) "UTF-8")))
      (interpose "&")
      (apply str))))

(defn url
  ([base]
    (str "http://localhost:3000" base))
  ([base q]
    (str base "?" (make-query-string q))))

(defn school-link [text school]
  (html [:a {:href (url (str "/schools/" (:id school)))} text]))

(defn wlt-a [schedule]
  (cond
    (> (-> schedule :result :a_score) (-> schedule :result :b_score))
      (html [:span {:class "win"} "(W)"])
    (< (-> schedule :result :a_score) (-> schedule :result :b_score))
      (html [:span {:class "loss"} "(L)"])
    :default (html [:span {:class "tie"} "(T)"])))

(defn wlt-b [schedule]
  (cond
    (> (-> schedule :result :b_score) (-> schedule :result :a_score))
      (html [:span {:class "win"} "(W)"])
    (< (-> schedule :result :b_score) (-> schedule :result :a_score))
      (html [:span {:class "loss"} "(L)"])
    :default (html [:span {:class "tie"} "(T)"])))

(defn wlt [schedule school-id]
  (if (= (:a_school_id schedule) school-id) (wlt-a schedule) (wlt-b schedule)))

(defn score-a [schedule]
  (str (-> schedule :result :a_score) " - " (-> schedule :result :b_score)))

(defn score-b [schedule]
  (str (-> schedule :result :b_score) " - " (-> schedule :result :a_score)))

(defn score [schedule school-id]
  (if (= (:a_school_id schedule) school-id) (score-a schedule) (score-b schedule)))

(defn win-streaks [streaks]
  (html [:table
    (for [s streaks]
      [:tr
        [:td (school-link (-> s :school :display_name) (:school s))]
        [:td (:streak s)]
        [:td (if (:end_season s)
          (str (:start_season s) " - " (:end_season s))
          (:start_season s))]
        [:td (when (:break_schedule s)
          (let [break-opp (if (= (:school_id s) (-> s :break_schedule :a_school_id))
                           (-> s :break_schedule :b_school)
                           (-> s :break_schedule :a_school))]
            (str (:display_name break-opp)
                 " "
                 (wlt (:break_schedule s) (:id break-opp))
                 " "
                 (score (:break_schedule s) (:school_id s)))))]])]))

(defroutes main-routes
  (GET "/all-time-win-streaks" [o l] (-> gs/query
      (gs/win)
      (model/for-list o l)
      (model/run)
      (win-streaks))))

(def app
  (handler/site main-routes))
