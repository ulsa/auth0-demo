(defproject auth0-demo "0.1.0-SNAPSHOT"
  :description "Simple Clojure authentication demo for Auth0"
  :url "https://github.com/ulsa/auth0-demo"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]

                 ;; added
                 [buddy "1.1.0"]                            ;authentication framework
                 [environ "1.1.0"]                          ;environment variables
                 [clj-http "3.3.0"]                         ;http calls to auth0
                 [hiccup "1.0.5"]                           ;html rendering
                 [com.taoensso/timbre "4.7.4"
                  :exclusions [org.clojure/tools.reader]]   ;logging
                 [cheshire "5.6.3"]                         ;json
                 [luminus/ring-ttl-session "0.3.1"]         ;basic session store
                 ]
  :plugins [[lein-ring "0.9.7"]

            ;; added
            [lein-environ "1.1.0"]                          ;easier env var handling in dev
            ]
  ;; added
  :uberjar-name "auth0-demo.standalone.jar"
  :aliases {"uberjar" ["ring" "uberjar"]}

  :ring {
         ;; added
         :init    auth0-demo.handler/init                   ;check required env variables

         :handler auth0-demo.handler/app}
  :profiles
  {

   ;; changed because external dev profiles overwrite this dev profile
   :dev          [:dev-overrides :dev-defaults]
   :dev-defaults {:dependencies [[javax.servlet/servlet-api "2.5"]
                                 [ring/ring-mock "0.3.0"]]}})
