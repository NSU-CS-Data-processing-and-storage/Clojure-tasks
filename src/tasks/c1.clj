(ns tasks.c1)

;; берем последний символ строки
(defn- last-ch ^String [^String s]
  (when (pos? (count s))
    (.substring s (dec (count s)))))

;; ко всем строкам acc дописывается по букве из alphabet
(defn- extend-once
  [alphabet acc]
  (reduce
   (fn [res s]
     (let [lc (last-ch s)]
       (concat res
               (map #(str s %)
                    (remove (fn [l] (= l lc)) alphabet)))))
   '()
   acc))

(defn strings-no-equal-adj
  [alphabet n]
  (cond
    (neg? n) (throw (IllegalArgumentException. "n must be >= 0"))
    (zero? n) '("")
    (empty? alphabet) '()
    :else
    (reduce (fn [acc _] (extend-once alphabet acc))
            '("")
            (range n))))
