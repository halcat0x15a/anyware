(ns anyware.jvm.twitter
  (:require [clojure.java.browse :refer [browse-url]]
            [anyware.core :as core]
            [anyware.core.api :as api]
            [anyware.core.buffer :as buffer]
            [anyware.core.minibuffer :as minibuffer])
  (:import [java.io
            File
            FileOutputStream ObjectOutputStream
            FileInputStream ObjectInputStream]
           [javafx.scene.input Clipboard ClipboardContent]
           twitter4j.Twitter
           twitter4j.TwitterFactory
           twitter4j.TwitterStreamFactory
           twitter4j.UserStreamAdapter))

(def consumer
  {:key "dLI8s1c0ZCGiKSdCmOyg"
   :secret "woolS6Ytmb7EJIC8zkshPMUtmVgKFCdw1svkDZEY7Wo"})

(def access-token (atom ".access_token"))

(defn pretty [status]
  (let [user (.getUser status)]
    (format "%s(%s)\n%s\n"
            (.getName user)
            (.getScreenName user)
            (.getText status))))

(def listener
  (proxy [UserStreamAdapter] []
    (onStatus [status]
      (swap! core/editor api/insert (pretty status)))))

(defn store [token]
  (doto (-> @access-token FileOutputStream. ObjectOutputStream.)
    (.writeObject token)
    .close))

(defn client []
  (doto (TwitterFactory/getSingleton)
    (.setOAuthConsumer (:key consumer) (:secret consumer))))

(defn restore []
  (-> @access-token FileInputStream. ObjectInputStream. .readObject))

(defn start
  ([token editor]
     (start (client) token editor))
  ([^Twitter twitter token editor]
     (store token)
     (.setOAuthAccessToken twitter token)
     (doto (.getInstance (TwitterStreamFactory.))
       (.setOAuthConsumer (:key consumer) (:secret consumer))
       (.setOAuthAccessToken token)
       (.addListener listener)
       .user)
     editor))

(defn access [^Twitter twitter request editor]
  (let [pin (-> editor (get-in api/minibuffer) buffer/write)
        token (.getOAuthAccessToken twitter request pin)]
    (start twitter token editor)))

(defn request [editor]
  (let [twitter (client)
        request (.getOAuthRequestToken twitter)
        url (.getAuthorizationURL request)]
    (doto (Clipboard/getSystemClipboard)
      (.setContent (doto (ClipboardContent.) (.putString url))))
    (-> editor
        (api/insert url)
        (assoc-in api/mode (assoc minibuffer/keymap
                             :enter (partial access twitter request))))))

(defn twitter [editor]
  (if (.exists (File. @access-token))
    (start (restore) editor)
    (request editor)))
