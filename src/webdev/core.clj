(ns webdev.core
  ;; We import our model code 
  (:require [webdev.item.model :as items])
  (:require
            ;; ring.adapter.jetty is an adapter we can use in dev and production
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            ;; handle-dump helps us to see the request in a nice format
            ;; It's useful as a debugging tool
            [ring.handler.dump :refer [handle-dump]]))

;; We store the connection url for the database
(def db "jdbc:postgresql://localhost/webdev")

(defn greet [req]
  {:status 200
   :body "Hello, world!"
   :headers {}})

(defn goodbye [req]
  {:status 200
   :body "Goodbye, cruel world!"
   "headers" {}})

(defn about [req]
  {:status 200
   :body "Hello, I'm Mi-Mi Na. I love the beauty of Geometry"
   :headers {}})

(defn yo [req]
  (let [name (get-in req [:route-params :name])]
   {:status 200
   :body (str "Yo!" name "!")
   :headers {}}))

(def ops
  {"+" +
   "-" -
   "*" *
   ":" /})

(defn calc [req]
  (let [a (Integer. (get-in req [:route-params :a]))
        b (Integer. (get-in req [:route-params :b]))
        op (get-in req [:route-params :op])
        f (get ops op)]
   (if f
     {:status 200
      :body (str (f a b))
      :headers {}}
     {:status 404
      :body (str "Unknown operator: " op)
      :headers {}})))


(defroutes app
  (GET "/" [] greet)
  (GET "/goodbye" [] goodbye)

  ;; Compojure can also have variable paht elements in a routing path
  ;; we specify a variable segment by starting it with a colon
  ;; They will be added to the key :route-params, always as a string
  (GET "/yo/:name" [] yo)
  
  (GET "/calc/:a/:op/:b" [] calc)
  (GET "/about" [] about)
  
  (GET "/request" [] handle-dump)
  (not-found "Page not found"))

;; This is our main function and it runs the jetty adapter
;; run-jetty takes a handler and an options map.
(defn -main [port]
  ;; We add  a call to the create-table function in the -main to create
  ;; the table if it doesn't exist before we start the adapter
  (items/create-table db)
  (jetty/run-jetty app                 {:port (Integer. port)}))

;; Este -main will be only called in development
;; We use here wrap-reload to wrap the handler. We refer to the app
;; var and not its value directly, so that when it gets redefined
;; the changes will be available to the adapter
(defn -dev-main [port]
  (items/create-table db)
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))
