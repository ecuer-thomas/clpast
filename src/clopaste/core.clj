(ns clopaste.core
  (:use [compojure.route :only [files not-found] :as route]
        [compojure.handler :only [site]]
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        [clojure.tools.logging :as log]
        org.httpkit.server)
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clopaste.conf  :as conf]
            [clopaste.misc  :as misc])
  (:gen-class))


(use 'clojure.pprint)
(def running (atom true))
(def http-domain "http://localhost:8080")
(def usage-space (atom 0))
(defonce server (atom nil))


(defn landing-page
  [req]
  (misc/log-request req)  
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    misc/landing-page})



;;
;; File management - tracking & deleting
;;




;; { id: expiration &things }
(def all-files (atom {}))

(defn reload-files
  "for each uploaded files,
  reads writing date on filesystem,
  and store it into 'files' atom"
  []
  (let [directory (clojure.java.io/file conf/static-location)
        files     (file-seq directory)
        filenames (rest (map #(.getName %1) files))
        sorted-a  (sort filenames)
        sorted    (map keyword sorted-a)
        dict      (zipmap sorted (cycle [{:count 10 :location "/"}]))
        sizes     (map #(.length %) files)
        size      (reduce + 0 sizes)]
    (swap! usage-space + size)
        ;last-log  (last sorted)
        ;indice    (last (string/split last-log #"\."))
                                        ;next-ind  (+ (read-string indice) 1)]
    (swap! all-files (fn [current] dict))))

(defn delete-file
  "sides effects. Delete file"
  [uid]
  (clojure.java.io/delete-file (str conf/static-location (name uid)))
  (let [freeable (-> @all-files uid :size)]
    (if-not (nil? freeable)
      (swap! usage-space - freeable)))
  (swap! all-files dissoc uid))

(defn to-del? [fileid]
  (< (-> @all-files fileid :count ) 0))

(defn clean-job []
  "Job dedicated for deleting files.
  It's done without file reading/stating
  by decrementing a counter into @all-files"
  (log/debug "CLEAN-JOB: Starting cleaner")  
  (while @running
    (do
      (let [upfiles @all-files]
        (swap!
         all-files
         ;; decrement :count's value for each tracked file.
         (fn [old]
           (into {} (map (fn [doc]
                  (update-in doc [1 :count] dec))
                old)))))

      
      ;; delete files
      (let [deletedn (mapv (fn [item]
              (if (to-del? (first item))
                (delete-file (first item)) nil))
                          @all-files)
            deleted (remove nil? deletedn)]
        (if-not (= 0 (count deleted))
          (do
            (log/debug (str "CLEAN-JOB: deleted " (count deleted) " files :"))
            (log/debug (str (string/join "\n" (map first deleted)))))
          (log/debug "CLEAN-JOB: nothing to do.")))

      (Thread/sleep (* (60 * 5) 1000))))) ;; every 5 minutes

;;
;; File management - writing
;;


(defn random-uuid [] (first (string/split (str (java.util.UUID/randomUUID)) #"-")))

(defn write-file-to-fs
  "write file to disk & return full url"
  [content length]
  (let [uuid (random-uuid)]
      (try

        (swap! all-files assoc (keyword uuid) {:count 10
                                           :location (str conf/static-location uuid)
                                           :size length})
        (clojure.java.io/copy
         content
         (java.io.File. (str conf/static-location uuid)))
        (swap! usage-space + length)
        uuid
        (catch Exception e
          (do
            (log/error (str "Got exeption: " e "\n" ":uuid" uuid))
            "/")))))


;;
;; HTTP-shortcut
;;

(defn make-request [status ] )

;;
;; HTTP Enpoints
;;


(defn post-doc
  "endpoint called when user post a document"
  [req]
  (let [content (-> :body req)]
                                        ;(let [content ((-> :multipart-params req)  "f:1")]
    (if-not (nil? content)
      {:status  201
       :headers {"Content-Type" "text/html"}
       :body    (str http-domain
                     "/up/"
                     (write-file-to-fs content (req :content-length))
                     "\r\n")}
      {:status  400
       :headers {"Content-Type" "text/html"}
       :body    "Bad request"})))


(defn post-doc-check-size
  [req]
  (misc/log-request req)
  ;;(log/info (str req))
  (if (< (- conf/max-size 1) (req :content-length))
    {:status  400
     :headers {"Content-Type" "text/html"}
     :body    "File too big"}
    (if (< conf/max-usage (+ (req :content-length) @usage-space))
      {:status  400
       :headers {"Content-Type" "text/html"}
       :body    "Server is full"}
      (post-doc req))))
    

(defroutes all-routes
  (GET "/"  []  landing-page)
  (POST "/" []  post-doc-check-size)

  (route/files "/up/") ;; static file url prefix /static, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404


(defn stop-server []
  (log/info (str @usage-space
                 "/"
                 conf/max-usage " Used"))
  (log/info (str (count @all-files)
                 " files"))

  (log/info "Gracefull shutdown.")
  (swap! running #(and % false))
  (when-not (nil? @server)
    (@server :timeout 1000)
    (reset! server nil)))

(.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if-not @running
    (future
      (do
        (reload-files)
        (clean-job))))

  (swap! running #(or % true))
  (reset! server (run-server #'all-routes {:port 8080}))
  ;;(run-server (site #'all-routes) {:port 8080})
  (log/info "Server started."))


