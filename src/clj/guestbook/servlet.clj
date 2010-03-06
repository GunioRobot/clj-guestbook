(ns clj.guestbook.servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use [compojure.http servlet routes])
  (:use compojure.html)
  (:import
    (com.google.appengine.api.users UserServiceFactory User)))

(defroutes clj-guestbook
  (GET "/clj/hello"
    (html [:h1 "Hola, petardo!"]))

  (GET "/clj/user"
    (let [user-service (UserServiceFactory/getUserService)
          user (.getCurrentUser user-service)]
      (html [:h1 "Hello, " (if user (.getNickname user) "World") "!"])))

  (GET "/clj/user/hello"
     ( let [user-service (UserServiceFactory/getUserService)
            user (.getCurrentUser user-service)]
      (html
        [:h1 "Hello, " (if user (.getNickname user) "World") "!"]
        [:p (link-to (.createLoginURL user-service "/clj/user") "sign in")]
        [:p (link-to (.createLogoutURL user-service "/clj/user") "sign out")]))))


(defservice clj-guestbook)
