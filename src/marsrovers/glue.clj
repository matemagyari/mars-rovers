(ns ^{:author mate.magyari
      :doc "The glue exposes messaging and component abstractions. It hides core.async from the rest of the application."}
  marsrovers.glue
  (:require [clojure.core.async :as a]))

;; -----------------  private functions ------------------------

(declare send-msg!)

(defn- process-result!
  "state - reseting the atom to this state
   msgs - messages to send
   effects - effects to execute. These are functions with deliberate side-effect and nil return value"
  [component-atom {state :state
                   msgs :msgs
                   effects :effects}]
  (when state
    (reset! component-atom state))
  (when effects
    (doseq [e! effects] (e!)))
  (when msgs
    (doseq [msg msgs] (send-msg! msg))))

;; -----------------  public functions ------------------------

(defn chan
  "Creates a channel. The rest of the applicatio doesn't have to know that it is a core.async channel."
  [] (a/chan 100))

(defn close-channel! [chan]
  (a/close! chan))

(defn start-component!
  "Starts up a component. A component is composed of:
   init-state - the init state. It contains the in-channel for the component
   msg-processing-fn - a function describing the component's behaviour.
                      Its signature is (State,Message)->{:state State :effects EffectsToExexute :msgs MessagesToSend}"
  [init-state msg-processing-fn]
  {:pre [(some? init-state) (some? (:in-channel init-state)) (some? msg-processing-fn)]}
  (let [component-atom (atom init-state)
        in-channel (:in-channel init-state)
        go-chan (a/go-loop []
                  (if-let [in-msg (a/<! in-channel)]
                    (let [result (msg-processing-fn @component-atom in-msg)]
                      (process-result! component-atom result)
                      (recur))
                    (println "Channel closed")))]
    ;(println "Component started up")
    ))


(defn start-simple-component!
  "Starts up a simple component, where state is not maintained. A simple component is composed of:
   in-channel - the channel the component listens to
   msg-processing-fn - a function describing the component's behaviour"
  [in-channel msg-processing-fn!]
  (a/go-loop []
    (if-let [in-msg (a/<! in-channel)]
      (do
        (msg-processing-fn! in-msg)
        (recur))
      (println "Channel closed"))))

(defn send-msg! [{:keys [body target delay] :as msg}]
  "Sends a message
   body - the message content
   target - the channel to send
   delay - delay in milliseconds, optional"
  {:pre [(some? target) (some? body)]}
  (a/go
    (when delay
      (a/<! (a/timeout (* delay 10))))
    (try
      (a/>! target body)
      (catch AssertionError err
        (println (str "AssertionError on message" msg (.getMessage err)))))))

(defn send-msg!-old [{:keys [body target delay] :as msg}]
  {:pre [(some? target) (some? body)]}
  (when delay
    (a/<!! (a/timeout (* delay 10))))
  (try
    (a/>!! target body)
    (catch AssertionError err
      (println (str "AssertionError on message" msg (.getMessage err))))))