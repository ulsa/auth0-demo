(ns auth0-demo.middleware
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [environ.core :refer [env]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring-ttl-session.core :refer [ttl-memory-store]]
            [ring.util.response :as response]
            [taoensso.timbre :as log]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t)
        (response/response {:status  500
                            :title   "Something very bad has happened!"
                            :message "We've dispatched a team of technicians to take care of the problem."})))))

(defn on-error [request _]
  (let [current-url (:uri request)
        redirect-uri (str "/login?next=" current-url)]
    (log/info "Not authenticated. Redirecting to" redirect-uri)
    (response/redirect redirect-uri)))

(defn wrap-restricted [handler]
  (restrict handler {:handler  authenticated?
                     :on-error on-error}))

(defn wrap-auth [handler]
  (let [backend (session-backend)]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defn wrap-base [handler]
  (-> handler
      wrap-auth
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:session :store] (ttl-memory-store (* 60 30)))))
      wrap-internal-error))
