(defproject anyware "0.1.0-SNAPSHOT"
  :description "text editor"
  :url "https://github.com/halcat0x15a/anyware"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/tools.nrepl "0.2.2"]
                 [org.clojure/test.generative "0.4.0"]
                 [clj-stacktrace "0.2.5"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :resource-paths ["/usr/lib/jvm/javafx-sdk/rt/lib/jfxrt.jar"]
  :injections [(let [orig (ns-resolve (doto 'clojure.stacktrace require)
                                      'print-cause-trace)
                     new (ns-resolve (doto 'clj-stacktrace.repl require)
                                     'pst+)]
                 (alter-var-root orig (constantly @new)))]
  :cljsbuild {:builds [{}]
              :crossovers [anyware.core]}
  :main anyware.jvm)
