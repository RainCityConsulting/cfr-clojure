(ns cfr.web.layout
  (:use [hiccup.core        ]
        [hiccup.page-helpers]))

(defn- full-layout [resp]
  (html
    (doctype :html5)
    (xhtml-tag "en"
      [:head
        [:title (if (:title resp)
                    (str (:title resp) " | College Football Reference")
                    "College Football Reference")]]
      [:body (:body resp)])))

(defn wrap-layout [handler]
  (fn [req]
    (if-let [resp (handler req)]
      (assoc resp :body (full-layout resp)))))
