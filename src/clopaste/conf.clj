(ns clopaste.conf
  (:require [clojure.string :as string])
  (:gen-class))

(def max-size 4194304)          ;4M  (individual file)
(def max-usage (* max-size 10)) ;40M (max disk usage for app)
(def max-http-body  8388608)    ;8M  (request-body-size)
(def upload-max-size (if (> max-size max-http-body) max-http-body max-size))

;; Global configuration

(def lifetime 20) ; 20*5 minutes
(def static-location "/home/thomas/work/clopaste/clopaste/public/")

                 ;; without trailing '/'
(def http-domain "http://localhost:8080")







