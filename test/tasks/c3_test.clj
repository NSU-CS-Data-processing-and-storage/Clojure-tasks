(ns tasks.c3-test
  (:require [clojure.test :refer :all]
            [tasks.c3 :as c3]))

(defn heavy? [x]
  (Thread/sleep 1)
  (zero? (mod x 3)))

(deftest pfilter-basic
  (is (= (take 50 (filter even? (range 100)))
         (take 50 (c3/pfilter even? (range 100))))))

(deftest pfilter-lazy-infinite
  (let [s (c3/pfilter #(= 1 (mod % 5)) (range) {:block-size 100 :parallelism 4})]
    (is (= (take 20 (filter #(= 1 (mod % 5)) (range)))
           (take 20 s)))))

(deftest pfilter-order
  (let [xs  (range 0 100)
        res (c3/pfilter #(< % 50) xs {:block-size 16 :parallelism 4})]
    (is (= (filter #(< % 50) xs) (doall res)))))

(deftest pfilter-speed-demo
  (let [xs (range 400)
        t0 (System/nanoTime)
        _  (doall (filter heavy? xs))
        t1 (System/nanoTime)
        seq-ms (/ (double (- t1 t0)) 1e6)

        t2 (System/nanoTime)
        _  (doall (c3/pfilter heavy? xs {:block-size 50 :parallelism 8}))
        t3 (System/nanoTime)
        par-ms (/ (double (- t3 t2)) 1e6)]
    (println "seq ms =" seq-ms " | par ms =" par-ms)
    (is (<= par-ms seq-ms))))
