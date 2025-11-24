(ns tasks.c5
   (:require [clojure.pprint :as pp]))

 ;; --------------------------Счётчик попыток транзакций--------------------------

 (def ^:dynamic *tx-restarts*
   "Счётчик попыток STM-транзакций."
   (atom 0))

 (defn make-fork
   [id]
   (ref {:id id
         :busy? false
         :use-count 0}))

 (defmacro dosync-counted
   "dosync, который внутри транзакции увеличивает счётчик попыток."
   [& body]
   `(dosync
     (swap! *tx-restarts* inc)
     ~@body))

 ;; --------------------------Логика философа--------------------------

 (defn take-forks!
   "Взять обе вилки.
   Если хотя бы одна занята, повторяем попытку в цикле."
   [left-fork right-fork]
   (loop []
     (let [result
           (dosync-counted
            (if (or (:busy? @left-fork)
                    (:busy? @right-fork))
              ;; сигнализируем, что транзакция не захватила вилки
              :retry
              (do
                (alter left-fork  #(-> %
                                       (assoc :busy? true)
                                       (update :use-count inc)))
                (alter right-fork #(-> %
                                       (assoc :busy? true)
                                       (update :use-count inc)))
                :ok)))]
       (when (= result :retry)
         (Thread/yield)
         (recur)))))

 (defn put-forks!
   "Положить обе вилки на стол (сделать свободными)."
   [left-fork right-fork]
   (dosync-counted
    (alter left-fork assoc :busy? false)
    (alter right-fork assoc :busy? false)))

 (defn philosopher
   "Запускает философа с данным id."
   [id left-fork right-fork think-ms eat-ms rounds]
   (future
     (dotimes [_ rounds]
       (Thread/sleep think-ms)

       (take-forks! left-fork right-fork)

       (Thread/sleep eat-ms)

       (put-forks! left-fork right-fork))))

;; --------------------------Запуск эксперимента--------------------------

(defn run-simulation!
  "Запускает задачу «обедающих философов» и возвращает результаты."
  [{:keys [n-philosophers think-ms eat-ms rounds]}]
  (reset! *tx-restarts* 0)

  (let [forks (vec (map make-fork (range n-philosophers)))
        ;; философ i использует вилки i и (i+1) mod n
        start (System/nanoTime)
        philosophers (doall
                      (for [i (range n-philosophers)]
                        (let [left  (forks i)
                              right (forks (mod (inc i) n-philosophers))]
                          (philosopher i left right think-ms eat-ms rounds))))]
    (doseq [p philosophers] @p)
    (let [elapsed-ms (/ (- (System/nanoTime) start) 1e6)]
      {:n-philosophers n-philosophers
       :think-ms think-ms
       :eat-ms eat-ms
       :rounds rounds
       :time-ms elapsed-ms
       :tx-attempts @*tx-restarts*
       :forks (mapv deref forks)})))

