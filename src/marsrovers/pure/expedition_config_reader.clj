(ns
  ^{:author mate.magyari
    :doc "Reading up the expedition config. Currently it's hard coded, but could come from file for example."}
  marsrovers.pure.expedition-config-reader
  (:require [marsrovers.pure.rover :as r]))

;; -----------------  private functions ------------------------
(defn- lot-of-actions
  "Creates a finite lazy sequence of rover actions"
  [n]
  (let [acc []]
    (if (zero? n)
      acc
      (cons (rand-nth [:left :move :right]) (lazy-seq (lot-of-actions (dec n)))))))

(defn- rand-rover-config [plateau-config action-number]
  (let [x (rand-int (:x plateau-config))
        y (rand-int (:y plateau-config))
        facing (rand-nth [:n :w :s :e])]
    (r/rover-config
      (r/rover-position x y facing)
      (lot-of-actions action-number)
      {:movement-speed 0 :turning-speed 0})))

(defn- rand-rover-configs [n action-number plateau-config]
  (->> #(rand-rover-config plateau-config action-number)
    repeatedly
    (take n)
    vec))

;; -----------------  public functions ------------------------

(defn expedition-config
  "Return the config for the expedition."
  [input-config]
  (let [default-config {:rover-number 100
                        :action-number 9999
                        :speed {:movement-speed 0
                                :turning-speed 0}
                        :plateau-config {:x 300
                                         :y 300}
                        :dim-screen [600 600]}
        result-config (merge default-config input-config)
        plateau-config {:x 300 :y 300}
        rover-number (get input-config :rover-number 10)
        action-number (get input-config :action-number 999999)]
    {:plateau-config plateau-config
     :rover-configs (rand-rover-configs rover-number action-number plateau-config)
     :dim-screen (:dim-screen result-config)}))
