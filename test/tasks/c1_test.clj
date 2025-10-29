(ns tasks.c1-test
  (:require [clojure.test :refer :all]
            [tasks.c1 :as c1]))

(deftest c1-basic-example
  (let [alphabet ["a" "b" "c"]
        res      (c1/strings-no-equal-adj alphabet 2)
        expected #{"ab" "ac" "ba" "bc" "ca" "cb"}]
    (is (= expected (set res))))
  (is (= 6 (count (c1/strings-no-equal-adj ["a" "b" "c"] 2)))))

(deftest c1-edges
  (is (= '("") (c1/strings-no-equal-adj ["x"] 0)))
  (is (= '()   (c1/strings-no-equal-adj []  3)))
  (let [ok? (fn [^String s]
              (every? (fn [[c1 c2]] (not= c1 c2))
                      (partition 2 1 s)))]
    (is (every? ok? (c1/strings-no-equal-adj ["a" "b" "c" "d"] 4)))))
