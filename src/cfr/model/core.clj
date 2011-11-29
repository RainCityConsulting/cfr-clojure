(ns cfr.model.core
  (:use [cfr.config :only [config]]
        [korma.db]
        [korma.core]))

(defdb db (mysql {
    :classname "com.mysql.jdbc.Driver"
    :subprotocol "mysql"
    :subname (str "//" (:db-host config) "/" (:db-name config))
    :user (:db-user config)
    :password (:db-pass config)}))

(defentity schools
  (transform (fn [s] (assoc s :__type :school))))

(defentity results)
(defentity divisions)

(defn find-by-id [ent id]
  (first (select ent (where {:id id}))))

(defentity schedules
  (transform (fn [s]
    (-> s
      (assoc :a_school (find-by-id schools (:a_school_id s)))
      (assoc :b_school (find-by-id schools (:b_school_id s)))
      (assoc :result (first (select results (where {:schedule_id (:id s)}))))))))

(defentity game-streaks
  (table :d_game_streaks)
  (transform (fn [s]
    (-> s
      (assoc :school (find-by-id schools (:school_id s)))
      (assoc :division (find-by-id divisions (:division_id s)))
      (assoc :start_schedule (find-by-id schedules (:start_schedule_id s)))
      (assoc :break_schedule (find-by-id schedules (:break_schedule_id s)))))))

(defn by [q n v] (where q (= n v)))
(defn for-list [q o l] (-> q (offset o) (limit l)))
(defn run [q] (exec q))

(defn count-star [q] (-> q (aggregate (count :*) :cnt) run first :cnt))

(defn pager [q off lim]
  {:size (count-star q)
   :vals (-> q (for-list off lim) (run))
   :offset off
   :limit lim})
