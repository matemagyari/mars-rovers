(ns
  ^{:author mate.magyari
    :doc "The app is on the top of the code. It knows the domain, takes the pure functions descibing the behaviour
          of the components and wires them together with the glue"}
  marsrovers.app
  (:require [marsrovers.pure.nasa-hq :as n]
            [marsrovers.pure.plateau :as p]
            [marsrovers.pure.rover :as r]
            [marsrovers.pure.api.rover-api :as ra]
            [marsrovers.pure.rover-controller :as c]
            [marsrovers.pure.util :as u]
            [marsrovers.glue :as glue]
            [marsrovers.monitor :as monitor]
            [marsrovers.display :as d]))

;; -----------------  private functions ------------------------
(defn- start-controller!
  "Starts up a mars rover controller"
  [controller-init-state]
  (glue/start-component!
    controller-init-state
    (fn [state in-msg]
      (c/receive state in-msg))))

(defn- start-nasa-hq!
  "Starts up the NASA HQ"
  [hq-init-state]
  (glue/start-component!
    hq-init-state
    (fn [state in-msg]
      (n/receive state in-msg start-controller!))))

(defn- start-plateau!
  "Starts up the Plateau"
  [plateau-init-state]
  (glue/start-component!
    plateau-init-state
    (fn [state in-msg]
      (p/receive state in-msg))))

(defn- start-displayer!
  "Starts up the displayer component - currently it's a SWING UI"
  [displayer-channel plateau-config dim-screen]
  (let [repaint! (d/repaint-fn [(:x plateau-config) (:y plateau-config)] dim-screen)
        sampler-repaint! (u/sampler-filter repaint! 100)]
    (glue/start-simple-component! displayer-channel sampler-repaint!)))


;; -----------------  public functions ------------------------
(defn start-world!
  "Starts up the world the rovers will roam"
  [expedition-config plateau-channel nasa-hq-channel displayer-channel dim-screen]
  (let [plateau-config (:plateau-config expedition-config)
        plateau-init-state (p/plateau plateau-config plateau-channel displayer-channel)
        nasa-hq-init-state (n/nasa-hq nasa-hq-channel)]
    (start-nasa-hq! nasa-hq-init-state)
    (start-plateau! plateau-init-state)
    (start-displayer! displayer-channel plateau-config dim-screen)))

(defn start-rover!
  "Starts up a single rover"
  [rover-init-state plateau-channel mediator-channel]
  (glue/start-component!
    rover-init-state
    (fn [rover-state in-msg]
      (r/receive rover-state in-msg plateau-channel mediator-channel)))
  (glue/send-msg! (u/msg (:in-channel rover-init-state) (ra/tick-msg (:id rover-init-state)))))


(defn start-rovers!
  "Starts up a bunch of rovers"
  [rover-configs plateau-channel mediator-channel]
  (let [rovers (for [i (range (count rover-configs))]
                      (r/rover i (nth rover-configs i) (glue/chan)))
        rovers-monitor-watch (monitor/rovers-monitor)]
    (doseq [rover rovers]
      ;(add-watch rover-atom :all-rovers rovers-monitor-watch)
      (start-rover! rover plateau-channel mediator-channel))))

