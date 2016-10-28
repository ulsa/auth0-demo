(ns auth0-demo.handler
  (:require [auth0-demo.layout :as layout]
            [auth0-demo.middleware :as middleware]
            [buddy.core.codecs.base64 :as b64]
            [buddy.sign.jws :as jws]
            [buddy.auth :as auth]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [compojure.core :refer [defroutes routes wrap-routes GET]]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [ring.util.response :as response]
            [taoensso.timbre :as log])
  (:import (java.net URLEncoder)))

(def port (Integer/parseInt (env :port "3000")))
(def domain (env :auth-domain))
(def client-id (env :auth-client-id))
(def client-secret (env :auth-client-secret))
(def callback-uri (env :auth-callback-uri (format "http://localhost:%d/callback" port)))
(def return-to-uri (env :auth-return-to-uri (format "http://localhost:%d/login" port)))

;; ## business logic

(defn user-authenticated [email auth0-user-id]
  (log/info "User" email "with id" auth0-user-id "authenticated")

  ;; do whatever is required with this authenticated user, eg store in db and return an internal id
  12345
  )

;; ## handlers

(defn show-login-page [next]
  (let [redirect-uri (URLEncoder/encode (if next
                                          (str callback-uri "?next=" next)
                                          callback-uri)
                                        "UTF-8")
        google-uri (str "https://" domain "/authorize"
                        "?response_type=code"
                        "&client_id=" client-id
                        "&connection=google-oauth2"
                        "&redirect_uri=" redirect-uri
                        "&scope=openid user_id email email_verified name")]
    (layout/render-login {:google-uri google-uri})))

(defn handle-login [code state next {:keys [session]}]
  ;; Exercise for the reader: check 'state' against some true value to prevent CSRF
  (log/debug "Checking authorization code" code)
  (let [url (str "https://" domain "/oauth/token")
        resp (client/post url
                          {:form-params {:client_id     client-id
                                         :client_secret client-secret
                                         :code          code
                                         :redirect_uri  callback-uri
                                         :grant_type    "authorization_code"}})
        id-token (-> resp
                     :body
                     (json/parse-string true)
                     :id_token)
        _ (log/debug "Authorization code" code "is good, received id_token with header" (jws/decode-header id-token))
        payload (some-> id-token
                        (jws/unsign (b64/decode client-secret))
                        String.
                        (json/parse-string true))
        profile (select-keys payload [:user_id :email :email_verified :name])
        {:keys [email auth0-user-id] :as profile} (clojure.set/rename-keys profile {:user_id        :auth0-user-id
                                                                                    :email_verified :email-verified})
        next-uri (or next "/")]
    (if payload
      (do
        (log/debug "JWT token is good, payload is" payload)
        (if email
          (if-let [internal-user-id (user-authenticated email auth0-user-id)]
            (do
              (log/infof "User %s with email %s is authenticated as %s, redirecting to %s" auth0-user-id email internal-user-id next-uri)
              (-> (response/redirect next-uri)
                  (assoc :session (assoc session :identity (assoc profile :user-id internal-user-id)))))
            (auth/throw-unauthorized {:message (str "Failed to authenticate user " auth0-user-id " with email " email)
                                      :profile profile}))
          (auth/throw-unauthorized {:message "No email in payload"
                                    :payload payload})))
      (auth/throw-unauthorized {:message  "JWT payload failed to parse"
                                :id-token id-token}))))

(defn handle-logout [{:keys [session]}]
  (if-let [user (:identity session)]
    (log/info "Logging out user" user)
    (log/info "No user in session, logging out regardless"))
  (-> (response/redirect (format "https://%s/v2/logout?returnTo=%s&client_id=%s"
                                 domain
                                 (URLEncoder/encode return-to-uri "UTF-8")
                                 (URLEncoder/encode client-id "UTF-8")))
      (assoc :session {})))

(defn show-home-page [{:keys [session]}]
  (layout/render-home {:profile (:identity session)}))

;; ## routes

(defroutes login-routes
           (GET "/login" [next] (show-login-page next))
           (GET "/logout" [:as req] (handle-logout req))
           (GET "/callback" [code state next :as req] (handle-login code state next req)))

(defroutes home-routes
           (GET "/" [:as req] (show-home-page req)))

(def app-routes
  (routes (-> #'login-routes)
          (-> #'home-routes
              (wrap-routes middleware/wrap-restricted))
          (route/not-found
            (:body
              (response/not-found "Sorry, page not found")))))

(def app
  (middleware/wrap-base #'app-routes))

(defn init []
  (log/set-level! :debug)
  (if-not (and (or domain (log/fatal "AUTH_DOMAIN needs to be set to an Auth0 domain, eg 'mydomain.auth0.com'"))
               (or client-id (log/fatal "AUTH_CLIENT_ID needs to be set to an Auth0 client id"))
               (or client-secret (log/fatal "AUTH_CLIENT_SECRET needs to be set to the Auth0 client secret")))
    (System/exit 1)))
