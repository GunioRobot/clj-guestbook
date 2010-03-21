(ns clj.guestbook.greetings
  (:require [appengine-clj.datastore :as ds])
  (:import (com.google.appengine.api.datastore Query)))

(defn create [content author]
  (ds/create {:kind "Greeting" :author author :content content :date (java.util.Date.)}))

(defn delete [keys] (ds/delete keys ))

(defn find-all []
  (ds/find-all (doto (Query. "Greeting") (.addSort "date"))))
