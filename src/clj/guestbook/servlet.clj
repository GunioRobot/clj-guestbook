(ns clj.guestbook.servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use [compojure.http servlet routes helpers])
  (:use compojure.html)
  (:require [clj.guestbook.greetings :as greetings])
  (:require [appengine-clj.users :as users])
  (:import
    (com.google.appengine.api.users UserServiceFactory User)))

;(defn path ([file] (str "/" file)))

(defn sign-guestbook [params user]
  (greetings/create (params :content) user)
   (redirect-to "/clj/guestbook"))

(defn show-guestbook [{:keys [user user-service]}]
  (let [all-greetings (greetings/find-all)]
    (html [:html [:head [:title "Guestbook"]]
      [:body
        (if user
          [:p "Hello, " (.getNickname user) "! (You can "
            (link-to (.createLogoutURL user-service "/clj") "sign out")
            ".)"]
          [:p "Hello! (You can "
            (link-to (.createLoginURL user-service "/clj") "sign in")
            " to include your name with your greeting when you post.)"])
        (if (empty? all-greetings)
          [:p "The guestbook has no messages."]
          (map (fn [greeting]
            [:div
             [:p (if (greeting :author) [:strong (.getNickname (greeting :author))] "An anonymous guest") " wrote:"]
              [:blockquote (h (greeting :content))]])
            all-greetings))
        (form-to [:post "/clj/sign"]
          [:div (text-area "content" "")]
          [:div (submit-button "Post Greeting")]) (link-to "/" "Post with that obsolete lang")]])))

(defroutes clj-guestbook
  ; Serve static resources
  ;(ANY "/*" (java.io.File. (path (params :*))))
  ;(ANY "/" (java.io.File. (path "index.html")))
  
  (GET "/clj/user2"
    (let [user-info (request :appengine-clj/user-info)
          user nil  c '(user-info :user)]
      (html
       [:h1 (.getEmail  user-info)]
       [:h1 "Hello, " (if user (.getNickname user) "World") "!"]
       (comment [:p (link-to (.createLoginURL (user-info :user-service) "/clj/user") "sign in")]
                [:p (link-to (.createLogoutURL (user-info :user-service) "/clj/user") "sign out")]))))
  
  (GET "/clj/hello"
    (html [:h1 "Hola, desconocido!"]))

  (GET "/clj/user/hello"
    (let [user-service (UserServiceFactory/getUserService)
          user (.getCurrentUser user-service)]
      (html [:h1 "Hello, " (if user (.getNickname user) "World") "!"])))

  (GET "/clj/user"
     ( let [user-service (UserServiceFactory/getUserService)
            user (.getCurrentUser user-service)]
      (html
        [:p (link-to (.createLoginURL user-service "/clj/user/hello") "sign in")]
        [:p (link-to (.createLogoutURL user-service "/clj/hello") "sign out")])))
  
  (POST "/clj/sign"
       (sign-guestbook params ((request :appengine-clj/user-info) :user)))
  (GET "/clj/guestbook"
       (show-guestbook (request :appengine-clj/user-info))))
  
(defservice (users/wrap-with-user-info clj-guestbook))
