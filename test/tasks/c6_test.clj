(ns tasks.c6-test
  (:require [clojure.test :refer :all]
            [tasks.c6 :as c6]))

(deftest same-city-zero-price
  (testing "Из города в тот же город — нулевая цена и пустой путь"
    (is (= {:path '(), :price 0}
           (c6/book-tickets c6/empty-map "City1" "City1")))))

(deftest simple-direct-route
  (testing "Простой маршрут A -> B бронируется с правильной ценой"
    (let [m (-> c6/empty-map
                (c6/route "A" "B" 100 3))
          res (c6/book-tickets m "A" "B")]
      (is (nil? (:error res)))
      (is (= 100 (:price res)))
      (is (= '("B") (:path res))))))

(deftest cheaper-multi-hop-than-direct
  (testing "Выбирается более дешёвый маршрут с пересадками"
    (let [m (-> c6/empty-map
                (c6/route "A" "C" 300 5)
                (c6/route "A" "B" 100 5)
                (c6/route "B" "C" 100 5))
          res (c6/book-tickets m "A" "C")]
      (is (nil? (:error res)))
      (is (= 200 (:price res)))
      (is (= '("B" "C") (:path res))))))

(deftest no-route-or-no-tickets
  (testing "Если маршрута нет или нет билетов, возвращается :error"
    (let [m1 c6/empty-map
          r1 (c6/book-tickets m1 "A" "B")]
      (is (:error r1)))

    (let [m2 (-> c6/empty-map
                 (c6/route "A" "B" 100 0))
          r2 (c6/book-tickets m2 "A" "B")]
      (is (:error r2)))))

(deftest tickets-decrease-and-not-below-zero
  (testing "Количество билетов уменьшается и не уходит в минус"
    (let [m (-> c6/empty-map
                (c6/route "A" "B" 100 1))
          r1 (c6/book-tickets m "A" "B")
          r2 (c6/book-tickets m "A" "B")]
      (is (nil? (:error r1)))
      (is (:error r2)))))

(deftest concurrent-booking-does-not-overuse-tickets
  (testing "При конкурентном бронировании суммарно продаётся не больше доступных билетов"
    (let [tickets-per-edge 2
          m                (-> c6/empty-map
                               (c6/route "A" "B" 100 tickets-per-edge))
          futures          (doall
                            (repeatedly 10
                                        #(future (c6/book-tickets m "A" "B"))))
          results          (map deref futures)
          successes        (filter #(nil? (:error %)) results)
          tickets-ref      (-> m :forward (get "A") (get "B") :tickets)]
      (is (<= (count successes) tickets-per-edge))
      (is (>= @tickets-ref 0)))))
