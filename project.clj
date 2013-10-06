(defproject anyware "0.1.0-SNAPSHOT"
  :description "text editor"
  :url "https://github.com/halcat0x15a/anyware"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/test.generative "0.5.1"]
                 [clj-stacktrace "0.2.5"]]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :resource-paths ["/opt/java/jre/lib/jfxrt.jar"]
  :cljsbuild {:builds [{}]
              :crossovers [anyware.core]}
  :main anyware.jvm)
