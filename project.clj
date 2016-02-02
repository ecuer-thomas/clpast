(defproject clopaste "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.18"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17"]
                 [compojure "1.4.0"]]
  :main ^:skip-aot clopaste.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
