(ns cfr.model.game-streaks
  (:use [cfr.model.core]
        [korma.db]
        [korma.core]))

(def query
  (-> (select* game-streaks)
    (order :streak :DESC)
    (order :start_season :DESC)
    (order :is_win :DESC)
    (order :division_id :ASC)
    (order :school_id :ASC)))

(defn win [q] (by q :is_win 1))
(defn loss [q] (by q :is_win 0))
(defn active [q] (by q :end_season nil))
(defn division-id [q v] (by q :division_id v))
(defn school-id [q v] (by q :school_id v))
