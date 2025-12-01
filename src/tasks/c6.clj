(ns tasks.c6)

(def transact-cnt (atom 0))

;;;an empty route map
;;;it is enough to use either forward or backward part (they correspond to each other including shared reference to number of tickets)
;;;:forward is a map with route start point names as keys and nested map as values
;;;each nested map has route end point names as keys and route descriptor as values
;;;each route descriptor is a map (structure in fact) of the fixed structure where 
;;;:price contains ticket price
;;;and :tickets contains reference to tickets number
;;;:backward has the same structure but start and end points are reverted 
(def empty-map
  {:forward {},
   :backward {}})

(defn route
  "Add a new route (route) to the given route map
   route-map - route map to modify
   from - name (string) of the start point of the route
   to - name (string) of the end poiunt of the route
   price - ticket price
   tickets-num - number of tickets available"
  [route-map from to price tickets-num]
  (let [tickets                (ref tickets-num :validator (fn [state] (>= state 0))),     ;reference for the number of tickets 
        orig-source-desc       (or (get-in route-map [:forward from]) {}),
        orig-reverse-dest-desc (or (get-in route-map [:backward to]) {}),
        route-desc             {:price   price,                                            ;route descriptor
                                :tickets tickets},
        source-desc            (assoc orig-source-desc to route-desc),
        reverse-dest-desc      (assoc orig-reverse-dest-desc from route-desc)]
    (-> route-map
        (assoc-in [:forward from] source-desc)
        (assoc-in [:backward to] reverse-dest-desc))))

;; -------------------------------
;; Dijkstra 
;; -------------------------------

(defn- dijkstra-route
  "Ищет кратчайший по цене маршрут из from в to.
   Учитывает только рёбра с tickets > 0.
   Возвращает:
   {:price    суммарная-стоимость
    :path     '(dest1 dest2 ... to)  ; список пунктов назначения (без from)
    :segments [{:from from
                :to   to
                :route-desc {:price ... :tickets <ref>}} ...]}"
  [route-map from to]
  (let [graph (:forward route-map)]
    (loop [dist      {from 0}              ; стоимость
           prev      {}
           prev-edge {}
           frontier  #{from}]             ; очередь необработанных вершин
      (if (empty? frontier)
        nil
        (let [u  (apply min-key dist frontier)
              du (dist u)]
          (if (= u to)
            ;; Восстановление пути и сегментов
            (let [result
                  (loop [cur   to
                         nodes [to]      ; вектор
                         segs  '()]
                    (if (= cur from)
                      (let [nodes' (reverse nodes)
                            path   (rest nodes')
                            segs'  (reverse segs)]
                        {:price    du
                         :path     path
                         :segments segs'})
                      (if-let [{:keys [from route-desc]} (prev-edge cur)]
                        (recur from
                               (conj nodes from)
                               (conj segs {:from       from
                                           :to         cur
                                           :route-desc route-desc}))
                        nil)))]
              result)
            (let [neighbors                          (get graph u)
                  [dist' prev' prev-edge' frontier'] (reduce
                                                      (fn [[dist prev prev-edge frontier] [v route-desc]]
                                                        (let [tickets-ref (:tickets route-desc)]
                                                          (if (pos? @tickets-ref)
                                                            (let [alt  (+ du (:price route-desc))
                                                                  best (get dist v Long/MAX_VALUE)]
                                                              (if (< alt best)
                                                                [(assoc dist v alt)
                                                                 (assoc prev v u)
                                                                 (assoc prev-edge v {:from       u
                                                                                     :to         v
                                                                                     :route-desc route-desc})
                                                                 (conj frontier v)]
                                                                [dist prev prev-edge frontier]))

                                                            [dist prev prev-edge frontier])))
                                                      [dist prev prev-edge (disj frontier u)]
                                                      (or neighbors {}))]
              (recur dist' prev' prev-edge' frontier'))))))))


(defn book-tickets
  "Tries to book tickets and decrement appropriate references in route-map atomically
   returns map with either :price (for the whole route) and :path (a list of destination names) keys 
          or with :error key that indicates that booking is impossible due to lack of tickets"
  [route-map from to]
  (if (= from to)
    {:path  '()
     :price 0}
    (dosync
     (swap! transact-cnt inc)
     (if-let [{:keys [path price segments]} (dijkstra-route route-map from to)]
       (do
         (doseq [{:keys [route-desc]} segments]
           (when-let [tickets-ref (:tickets route-desc)]
             (alter tickets-ref dec)))
         {:path  path
          :price price})
       {:error :no-available-route}))))

;;;cities
(def spec1 (-> empty-map
               (route "City1" "Capital"    200 5)
               (route "Capital" "City1"    250 5)
               (route "City2" "Capital"    200 5)
               (route "Capital" "City2"    250 5)
               (route "City3" "Capital"    300 3)
               (route "Capital" "City3"    400 3)
               (route "City1" "Town1_X"    50 2)
               (route "Town1_X" "City1"    150 2)
               (route "Town1_X" "TownX_2"  50 2)
               (route "TownX_2" "Town1_X"  150 2)
               (route "Town1_X" "TownX_2"  50 2)
               (route "TownX_2" "City2"    50 3)
               (route "City2" "TownX_2"    150 3)
               (route "City2" "Town2_3"    50 2)
               (route "Town2_3" "City2"    150 2)
               (route "Town2_3" "City3"    50 3)
               (route "City3" "Town2_3"    150 2)))

(defn booking-future [route-map from to init-delay loop-delay]
  (future
    (Thread/sleep init-delay)
    (loop [bookings []]
      (Thread/sleep loop-delay)
      (let [booking (book-tickets route-map from to)]
        (if (booking :error)
          bookings
          (recur (conj bookings booking)))))))

(defn print-bookings [name ft]
  (println (str name ":") (count ft) "bookings")
  (doseq [booking ft]
    (println "price:" (booking :price) "path:" (booking :path))))

(defn run []
  ;; try to tune timeouts in order to all the customers gain at least one booking 
  (reset! transact-cnt 0)
  (let [f1 (booking-future spec1 "City1" "City3" 0   1)
        f2 (booking-future spec1 "City1" "City2" 100 1)
        f3 (booking-future spec1 "City2" "City3" 100 1)]
    (print-bookings "City1->City3" @f1)
    (print-bookings "City1->City2" @f2)
    (print-bookings "City2->City3" @f3)
    (println "Total (re-)starts:" @transact-cnt)))

(defn -main [& _]
  (run))
