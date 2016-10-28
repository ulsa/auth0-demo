(ns auth0-demo.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [auth0-demo.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 302))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
