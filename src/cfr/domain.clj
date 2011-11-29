(ns cfr.domain
  (:require [clojure.string :as s]))

(defn- make-slug [s]
  (-> s
    (s/replace #"\p{Space}" "-")
    (s/replace #"[^\p{Alnum}-]" "")
    (s/lower-case)))

(defmulti slug :__type)

(defmethod slug :school [s]
  (make-slug (:display_name s)))
