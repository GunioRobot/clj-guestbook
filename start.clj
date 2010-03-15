; starting the application in a jetty.
; the original application is decorated by a function that sets up the
; app engine services.

(ns start
  (:use (clj.guestbook servlet greetings))
  (:use appengine-clj.users)
  (:use compojure.server.jetty compojure.http compojure.control)
  (:import (org.mortbay.jetty.webapp WebAppContext)
           (com.google.appengine.tools.development ApiProxyLocalFactory
                                                   LocalServerEnvironment)
           (com.google.apphosting.api ApiProxy ApiProxy$Environment)
           (com.google.appengine.api.users dev.LocalLogoutServlet
                                           dev.LocalLoginServlet
                                           dev.LoginCookieUtils
                                           UserServiceFactory)
           (guestbook Greeting GuestbookServlet SignGuestbookServlet)))

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
  (let [email (or (:email (:session request))
                  "jneira@test.com")]
    (proxy [ApiProxy$Environment] []
      (isLoggedIn [] (boolean email))
      (getAuthDomain [] "")
      (getRequestNamespace [] "")
      (getDefaultNamespace [] "")
      (getAttributes [] (java.util.HashMap.))
      (getEmail [] email)
      (isAdmin [] true)
      (getAppId [] "local"))))

(defn environment-decorator
  "decorates the given application with a local version of the app engine environment"
  [application]
   (fn [request]
     (with-app-engine (login-aware-proxy request)
       ((wrap-with-user-info application) request))))

(comment "This method is broken with gae sdk 1.3.1"
         (defn init-app-engine
           "Initialize the app engine services."
           ([] (init-app-engine "C:/tmp"))
           ([dir] (ApiProxy/setDelegate
                   (proxy [ApiProxyLocalImpl] [(java.io.File. dir)])))))

; from a comment in http://blog.miau.biz/2010/01/interactive-clojure-on-appengine-pt2.html
(defn init-app-engine
  "Initialize the app engine services."
  ([] (init-app-engine "/"))
  ([dir] (let [proxy-factory (ApiProxyLocalFactory.)
               environment (proxy [LocalServerEnvironment] []
                             (getAppDir [] (new java.io.File dir)))
               api-proxy (.create proxy-factory environment)]
           (ApiProxy/setDelegate api-proxy) api-proxy)))

(defmacro gae-servlet [svClass]
 `(proxy [~svClass] []
    (doGet [req# resp#]
           (with-app-engine (login-aware-proxy nil) (proxy-super doGet req# resp#)))
    (doPost [req# resp#]
            (with-app-engine (login-aware-proxy nil) (proxy-super doPost req# resp#)))))
;; make sure every thread has the environment set up

(defn start-it  [webappDir]
  (do (init-app-engine)
      (let [serv (jetty-server
                  {:port 8080}
                  "/" (servlet (environment-decorator clj-guestbook))
                  "/guestbook" (gae-servlet GuestbookServlet)
                  "/_ah/login" (new LocalLoginServlet)
                  "/_ah/logout" (new LocalLogoutServlet)
                  "/sign" (gae-servlet SignGuestbookServlet))
            urlDir (.toExternalForm (.toURL (new java.io.File webappDir)))]
        (comment .setHandler serv (new WebAppContext  urlDir "/"))
        (start serv) serv)))

(def server  (start-it "c:/Users/atreyu/dev/ws/clojure/clj-guestbook/war/"))
(require 'compojure)
(compojure/stop server)
