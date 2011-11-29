(ns cfr.web.html
  (:use [cfr.config              :only [config]]
        [hiccup.core])
  (:require [cfr.domain          :as d]
            [hiccup.page-helpers :as h]))

(defn- make-base-url [hostname port base]
  (str "http://" hostname (if (= "80" port) ("") (str ":" port)) base))

(def cfr-url-base
  (let [{hostname :cfr-url-hostname, port :cfr-url-port, base :cfr-url-base} config]
    (make-base-url hostname port base)))

(def url-base
  (let [{hostname :url-hostname, port :url-port, base :url-base} config]
    (make-base-url hostname port base)))

(defn append-query-param [uri & params]
  (let [parts (butlast uri) ps (last uri)]
    (if (map? ps)
      (conj (vec parts) (apply assoc ps params))
      (conj (vec uri) (apply assoc {} params)))))

(defn cfr-url [& args]
  (with-base-url cfr-url-base
    (apply h/url args)))

(defn schedule-link [text {:keys [id season a_school b_school]}]
  [:a {:href (cfr-url "/schedules/"
                      (d/slug a_school)
                      "/vs/" (d/slug b_school)
                      "/" season
                      "/" id)} text])

(defn school-link [text school]
  [:a {:href (cfr-url "/schools/" (d/slug school) "/" (:id school))} text])

(defn school-season-link [text school season]
  [:a {:href (cfr-url "/schools/"
                      (d/slug school)
                      "/" (:id school)
                      "/" season )} text])

(defn at [schedule school-id]
  (when (= school-id (:home_school_id schedule))
    "AT"))

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

(defn simple-pager [uri {off :offset, lim :limit, size :size} & body]
  (html [:div.simple-pager-container
          body
          [:div.simple-pager
            (if (= 0 off)
              [:div.control.prev.disabled "<--"]
              [:div.control.prev
                [:a {:href (apply h/url (append-query-param uri :o (max 0 (- off lim)), :l lim))} "<--"]])
            (if (< size (+ off lim))
              [:div.control.next.disabled "-->"]
              [:div.control.next
                [:a {:href (apply h/url (append-query-param uri :o (+ off lim), :l lim))} "-->"]])]]))

(defn win-streaks [streaks]
  (html [:table.sortable.list
    (for [{:keys [start_season end_season streak school break_schedule] :as s} streaks]
      [:tr
        [:td (school-link (:display_name school) school)]
        [:td streak]
        [:td (if end_season
                 (html (school-season-link start_season school start_season)
                       " - "
                       (school-season-link end_season school end_season))
                 (school-season-link start_season school start_season))]
        [:td (when break_schedule
          (let [break-opp (if (= (:school_id s) (:a_school_id break_schedule))
                            (:b_school break_schedule)
                            (:a_school break_schedule))]
            (html (at break_schedule (:id break-opp))
              " " (school-season-link (:display_name break-opp) break-opp end_season)
              " " (wlt break_schedule (:school_id s))
              " " (schedule-link (score break_schedule (:id school)) break_schedule))))]])]))
