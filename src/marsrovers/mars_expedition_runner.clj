(ns
  ^{:author mate.magyari
    :doc "Expedition runner"}
  marsrovers.mars-expedition-runner
  (:require [marsrovers.pure.util :as util]
            [marsrovers.app :as app]
            [marsrovers.glue :as glue]
            [marsrovers.pure.expedition-config-reader :as ecr]))

(defn- args->config
  "Transforms the command line input parameters to a config map"
  [args]
  (if args (read-string (str "{" (first args) "}")) {}))

(defn run-the-show!
  "Runs the show"
  [config]
  (let [displayer-channel (glue/chan)
        plateau-channel (glue/chan)
        nasa-hq-channel (glue/chan)
        time-stamp (System/currentTimeMillis)
        expedition-config (ecr/expedition-config config)]
    (do
      (println (str (- (System/currentTimeMillis) time-stamp) " ms has elapsed"))
      (println "Word starting...")
      (app/start-world! expedition-config plateau-channel nasa-hq-channel displayer-channel)
      (app/start-displayer! displayer-channel (:plateau-config expedition-config) (:dim-screen expedition-config))
      (println "Word started")
      (app/start-rovers!
        (:rover-configs expedition-config)
        plateau-channel
        nasa-hq-channel)
      (println "Rovers started up"))))

(defn -main [& args]
  (-> args args->config run-the-show!))

;(-main ":rover-number 100 :action-number 99999")


