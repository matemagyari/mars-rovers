(ns ^{:author mate.magyari
      :doc "Components"}
  marsrovers.component)

;; -----------------  private functions ------------------------

;; -----------------  public functions ------------------------

(defprotocol ComponentFactory
  (create-component [this state])
  (get-in-channel [this c])
  (update-state! [this c state])
  (get-state [this c]))

(def cf (reify ComponentFactory
          (create-component [this state]
            (atom state))
          (get-in-channel [this c]
            (:in-channel @c))
          (update-state! [this c new-state]
            (reset! c new-state))
          (get-state [this c]
            @c)))

(def x (create-component cf {:in-channel 4 :val 5}))
(get-in-channel cf x)
(update-state! cf x {:in-channel 3 :val 2})
(get-state cf x)

(defprotocol Component
  (get-in-channel [this])
  (update-state! [this state])
  (get-state [this]))

(defrecord AtomComponent [state-atom]
  Component
  (get-in-channel [this]
    (:in-channel @state-atom))
  (update-state! [this new-state]
    (reset! state-atom new-state))
  (get-state [this]
    @state-atom))


(def ac (->AtomComponent
          (atom {:in-channel "inchan" :val 2})))

(get-in-channel ac)
(get-state ac)
(update-state! ac {:in-channel "inchan" :val 4})
(get-state ac)
(update-state! ac {:in-channel "inchan" :val 5})
(get-state ac)

