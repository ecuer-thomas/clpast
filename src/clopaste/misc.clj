(ns clopaste.misc
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:use [clopaste.conf :only [http-domain upload-max-size]])
  (:gen-class))

(defn log-request [request]
  (let [{addr              :remote-addr
         port              :server-port
         host              :server-name
         {ua "user-agent"} :headers
         size              :content-length
         [method url]      :compojure/route} request]
    (log/info (str host ":" port
                   " from " addr
                   " (size " size ") - "
                   (string/upper-case (name method)) " - " url
                   " - user-agent: " ua))))


(def landing-page
  (str
   (string/join
    "\r\n"
    [ "\r\n"
      "Availables commands:"
      "===================="
      "\r\n"
      "\r\n"   
      "POST /       => upload a document. Returns url in plain text."
      "\r\n"
      "GET /up/:id  => download a document"
      "\r\n"
      "\r\n"
      "Examples:"
      "\r\n"
      ;; quick examples
      (str
       "$ ls -lh public/ | curl -XPOST -H \"Content-type: application/octet-stream\"  --data-binary @- "
       http-domain)
      (str
       "$ cat somefile | curl -XPOST -H \"Content-type: application/octet-stream\"  --data-binary @- "
       http-domain)

      "\r\n"
      "Quick alias"
      (str
       "$ alias internet=\"curl -XPOST -H 'Content-type: application/octet-stream'  --data-binary @- "
       http-domain
       "\"")
      "$ cat somefile | internet"
      "\r\n"
      "Limits:"
      "======="
      (str upload-max-size " bytes maximum per upload")])))



