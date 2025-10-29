(ns tasks.c1)

(defn- last-ch ^String [^String s]
  (when (pos? (count s))
    (.substring s (dec (count s)))))

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
