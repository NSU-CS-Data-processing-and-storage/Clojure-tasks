(ns tasks.c2)

(defn sieve
  [s]
  (lazy-seq
   (let [p (first s)]
     (cons p (sieve (remove #(zero? (mod % p)) (rest s)))))))

(def primes
  (sieve (iterate inc 2)))
