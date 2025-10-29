(ns tasks.c2-test
  (:require [clojure.test :refer :all]
            [tasks.c2 :as c2]))

(deftest primes-first-10
  (is (= [2 3 5 7 11 13 17 19 23 29]
         (take 10 c2/primes))))

(deftest primes-known-indices
  (is (= 541  (nth c2/primes 99)))
  (is (= 7919 (nth c2/primes 999))))

(deftest primes-are-prime
  (let [ps (take 50 c2/primes)]
    (is (every?
         (fn [p]
           (every? #(pos? (mod p %))
                   (take-while #(< % p) c2/primes)))
         ps))))
