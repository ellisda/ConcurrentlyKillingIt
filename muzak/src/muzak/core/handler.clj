(ns muzak.core.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]
            [chord.http-kit :refer [wrap-websocket-handler]]
            [clojure.core.async :refer [<! >! put! close! go-loop]]
            [hiccup.page :refer [html5 include-js include-css]]))

(defn page-frame []
  (html5
   [:head
    [:title "Muzak"]
    (include-js "/js/jquery-2.1.1.min.js")
    (include-js "/js/bootstrap.min.js")
    (include-js "/js/d3.min.js")
    (include-js "/js/main.js")
    (include-css "/css/bootstrap.min.css")
    (include-css "http://fonts.googleapis.com/css?family=Lobster")
    (include-css "/css/style.css")
    (include-js "/src-cljs/event_handlers.cljs")]
   [:body [:div#content]]))


;; Event Handler Functions
(defn parse-event-handler [data]
  (prn data))

;; Event listeners
(defn ws-handler [{:keys [ws-channel] :as req}]
  (println "Opened connection from" (:remote-addr req))
  (go-loop []
    (when-let [{:keys [message error] :as msg} (<! ws-channel)]

      ;; Retrieve event field of message
      (def event (get (get msg :message) :event))

      ;; Retrieve data field of message
      (def data (get (get msg :message) :data))

      ;; Perform callback associated with the event
      (cond
       (= event "parse") (parse-event-handler data)
       :else (prn "No event handler found"))

      (>! ws-channel (if error
                       (format "Error: '%s'." (pr-str msg))
                       {:received (format "You passed: '%s' at %s." (pr-str message) (java.util.Date.))}))
      (recur))))



(defroutes app-routes
  (GET "/" [] (response (page-frame)))
  (GET "/ws" [] (-> ws-handler
                    (wrap-websocket-handler {:format :json-kw})))
  (resources "/"))

(def app
  #'app-routes)
