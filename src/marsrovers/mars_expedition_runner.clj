(ns
  ^{:author mate.magyari
    :doc "Expedition runner"}
  marsrovers.mars-expedition-runner
  (:require [marsrovers.pure.util :as util]
            [marsrovers.app :as app]
            [marsrovers.glue :as glue]
            [marsrovers.pure.expedition-config-reader :as ecr]))

(defn -main [& args]
  (let [displayer-channel (glue/chan)
        plateau-channel (glue/chan)
        nasa-hq-channel (glue/chan)
        time-stamp (System/currentTimeMillis)
        input-pars (if args (read-string (str "{" (first args) "}")) {})
        expedition-config (ecr/expedition-config input-pars)
        dim-screen [600 600]]
    (do
      (println (str (- (System/currentTimeMillis) time-stamp) " ms has elapsed"))
      (println "Word starting...")
      (app/start-world! expedition-config plateau-channel nasa-hq-channel displayer-channel dim-screen)
      (println "Word started")
      (app/start-rovers!
        (:rover-configs expedition-config)
        plateau-channel
        nasa-hq-channel)
      (println "Rovers started up"))))

(-main ":rover-number 200")

