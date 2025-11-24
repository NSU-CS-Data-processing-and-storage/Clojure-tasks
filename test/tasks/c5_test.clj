(ns tasks.c5-test
  (:require [clojure.test :refer :all]
            [tasks.c5 :as c5]))

(deftest simulation-basic-test
  (testing "Базовый прогон симуляции не падает и даёт осмысленные результаты"
    (let [cfg {:n-philosophers 5
               :think-ms       5
               :eat-ms         5
               :rounds         10}
          {:keys [n-philosophers think-ms eat-ms rounds
                  time-ms tx-attempts forks] :as res}
          (c5/run-simulation! cfg)]

      (is (= 5 n-philosophers))
      (is (= 5 think-ms))
      (is (= 5 eat-ms))
      (is (= 10 rounds))

      ;; время > 0
      (is (number? time-ms))
      (is (pos? time-ms))

      ;; попытки транзакций неотрицательны
      (is (integer? tx-attempts))
      (is (>= tx-attempts 0))

      ;; вилок столько же, сколько философов
      (is (= 5 (count forks)))

      ;; каждая вилка имела ненулевое число использований
      (doseq [f forks]
        (is (map? f))
        (is (contains? f :id))
        (is (contains? f :use-count))
        (is (integer? (:use-count f)))
        (is (pos? (:use-count f)))))))

(deftest one-philosopher-test
  (testing "Один философ не уходит в дедлок и вилку реально использует"
    (let [cfg {:n-philosophers 1
               :think-ms       1
               :eat-ms         1
               :rounds         5}
          res (c5/run-simulation! cfg)
          forks (:forks res)]
      (is (= 1 (:n-philosophers res)))
      (is (= 1 (count forks)))

      (let [{:keys [use-count]} (first forks)]
        (is (pos? use-count))))))

(deftest more-contention-test
  (testing "Большее число философов даёт больше транзакций"
    (let [cfg-small {:n-philosophers 2
                     :think-ms       5
                     :eat-ms         5
                     :rounds         10}
          cfg-big   {:n-philosophers 7
                     :think-ms       5
                     :eat-ms         5
                     :rounds         10}
          res-small (c5/run-simulation! cfg-small)
          res-big   (c5/run-simulation! cfg-big)]
      (is (pos? (:time-ms res-small)))
      (is (pos? (:time-ms res-big)))

      (is (>= (:tx-attempts res-big) (:tx-attempts res-small))))))

