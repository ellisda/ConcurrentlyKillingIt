(defproject muzak "0.1.0-SNAPSHOT"
  :description "A visualizer for the Millon Song Dataset"
  :url "https://github.com/ryanmilvenan/ConcurrentlyKillingIt"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jarohen/chord "0.4.2" :exclusions [org.clojure/clojure]]

                 [ring/ring-core "1.2.0"]
                 [compojure "1.2.0"]
                 [hiccup "1.0.4"]

                 [org.clojure/core.async "0.1.301.0-deb34a-alpha"]
                 [org.clojure/clojurescript "0.0-2268"]

                 [prismatic/dommy "0.1.2"]
                 [net.drib/strokes "0.5.1"]

                 [jarohen/clidget "0.2.0" :exclusions [org.clojure/clojure]]

                 [lein-light-nrepl "0.1.0"]]

  :plugins [[lein-pdo "0.1.1"]
            [jarohen/lein-frodo "0.3.2"]
            [lein-cljsbuild "1.0.3"]
            [lein-shell "0.4.0"]]

  :frodo/config-resource "muzak-server-config.edn"

  :aliases {"start-server" ["do"
                   ["pdo"
                    ["cljsbuild" "auto"]
                    "frodo"]]}

  :source-paths ["src"]

  :resource-paths ["resources"
                   ;"resources/lib/cisd-args4j.jar"
                   ;"resources/lib/cisd-base.jar"
                   "resources/lib/cisd-jhdf5-core.jar"
                   "resources/lib/cisd-jhdf5-tools.jar"
                   "resources/lib/cisd-jhdf5.jar"
                   ;"resources/lib/commons-io.jar"
                   ;"resources/lib/commons-lang.jar"
                   "resources/lib/nativejar/hdf5-windows-intel.jar"]

  :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}

  :cljsbuild {
    :builds [{
      :source-paths ["src-cljs"]
      :compiler {
        :output-to "resources/public/js/main.js"
        :optimizations :whitespace
        :pretty-print true}}]})
