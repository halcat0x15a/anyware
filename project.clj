(defproject anyware "0.1.0-SNAPSHOT"
  :description "text editor"
  :url "https://github.com/halcat0x15a/anyware"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/tools.nrepl "0.2.0-RC1"]
                 [org.clojure/test.generative "0.4.0"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :resource-paths ["/usr/lib/jvm/javafx-sdk/rt/lib/jfxrt.jar"]
  :cljsbuild {:builds []
              :crossovers [anyware.core]}
  :aot [anyware.jvm]
  :main anyware.jvm)
