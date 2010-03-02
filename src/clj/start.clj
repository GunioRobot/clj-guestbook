; starting the application in a jetty.
; the original application is decorated by a function that sets up the
; app engine services.

(ns clj.start
  (:use clj.guestbook.servlet)
  (:use compojure.server.jetty compojure.http compojure.control)
  (:import (com.google.appengine.tools.development ApiProxyLocalFactory LocalServerEnvironment)
           (com.google.apphosting.api ApiProxy ApiProxy$Environment)))

(defmacro with-app-engine
  "testing macro to create an environment for a thread"
  ([body]
    `(with-app-engine env-proxy ~body))
  ([proxy body]
    `(last (doall [(ApiProxy/setEnvironmentForCurrentThread ~proxy)
    ~body]))))

(defn login-aware-proxy
  "returns a proxy for the google apps environment that works locally"
  [request]
  (let [email (:email (:session request))]
    (proxy [ApiProxy$Environment] []
      (isLoggedIn [] (boolean email))
      (getAuthDomain [] "")
      (getRequestNamespace [] "")
      (getDefaultNamespace [] "")
      (getAttributes [] (java.util.HashMap.))
      (getEmail [] (or email ""))
      (isAdmin [] true)
      (getAppId [] "local"))))

(defn environment-decorator
  "decorates the given application with a local version of the app engine environment"
  [application]
    (fn [request]
      (with-app-engine (login-aware-proxy request)
      (application request))))

(comment "This method is broken with gae sdk 1.3.1"
         (defn init-app-engine
           "Initialize the app engine services."
           ([] (init-app-engine "/tmp"))
           ([dir] (ApiProxy/setDelegate
                   (proxy [ApiProxyLocalImpl] [(java.io.File. dir)])))))
; from a comment in http://blog.miau.biz/2010/01/interactive-clojure-on-appengine-pt2.html
(defn init-app-engine
  "Initialize the app engine services."
  ([] (init-app-engine "/tmp"))
  ([dir] (let [proxy-factory (ApiProxyLocalFactory.)
               environment (proxy [LocalServerEnvironment] []
                             (getAppDir [] (java.io.File dir)))
               api-proxy (.create proxy-factory environment)]
           (ApiProxy/setDelegate api-proxy))))

;; make sure every thread has the environment set up

(defn start-it  []
  (do (init-app-engine)
    (run-server {:port 8080} "/*" (servlet (environment-decorator clj-guestbook)))))

(comment def server start-it)
(comment compojure/stop server)
