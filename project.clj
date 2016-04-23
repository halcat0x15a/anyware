(defproject anyware "0.1.0-SNAPSHOT"
  :description "text editor"
  :url "https://github.com/halcat0x15a/anyware"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/test.generative "0.5.2"]
                 [org.clojure/tools.nrepl "0.2.11"]]
  :global-vars {*warn-on-reflection* true}
  :aot [anyware.main]
  :main anyware.main)
