(ns auth0-demo.layout
  (:require [hiccup.core :refer [html h]]
            [taoensso.timbre :as log]))

(defn render-base
  [title body]
  (html [:html
         [:head
          [:link {:href        "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
                  :rel         "stylesheet"
                  :integrity   "sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
                  :crossorigin "anonymous"}]
          [:title title]]
         [:body body]]))

(defn render-login [context]
  (render-base "Login"
               [:div [:h3.text-center
                      "You're not authenticated"]
                [:p.text-center
                 [:a.btn.btn-primary {:href (:google-uri context)}
                  "Log in with Google"]]]))

(defn render-home [context]
  (log/debug "Rendering home with" context)
  (render-base "Home"
               [:div [:h3.text-center
                      (h (format "You're authenticated as %s <%s>"
                                 (get-in context [:profile :name])
                                 (get-in context [:profile :email])))]
                [:p.text-center
                 [:a.btn.btn-primary {:href "/logout"}
                  "Log out"]]]))
