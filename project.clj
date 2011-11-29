(defproject cfr "1.0.0"
  :description "CFR"
  :dependencies [
      [org.clojure/clojure "1.3.0"]
      [org.clojure/java.jdbc "0.1.1"]
      [clj-config "0.1.0"]
      [mysql/mysql-connector-java "5.1.6"]
      [korma "0.2.1"]
      [compojure "0.6.4"]
      [hiccup "0.3.7"]]
  :dev-dependencies [[lein-ring "0.4.5"]]
  :ring {:handler cfr.web.handler/app})
