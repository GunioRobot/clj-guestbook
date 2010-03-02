(ns clj.guestbook.servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use [compojure.http servlet routes])
  (:use compojure.html))

(defroutes clj-guestbook
  (GET "/clj/hello"
    (html [:h1 "Hello, World!"])))

(defservice clj-guestbook)
