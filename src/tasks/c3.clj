(ns tasks.c3)

(import 'clojure.lang.PersistentQueue)

(defn pfilter
  ([pred coll] (pfilter pred coll {}))
  ([pred coll {:keys [block-size parallelism]
               :or   {block-size  2048
                      parallelism (.availableProcessors (Runtime/getRuntime))}}]
   (let [chunks (partition-all block-size coll)
         submit! (fn [chunk] (future (doall (filter pred chunk))))]
     (letfn [(drain [q remaining]
               (lazy-seq
                (let [[q remaining]
                      (loop [q q, rem remaining]
                        (if (and (< (count q) parallelism) (seq rem))
                          (recur (conj q (submit! (first rem))) (rest rem))
                          [q rem]))]
                  (if (pos? (count q))
                    (let [res @(peek q)]
                      (concat res (drain (pop q) remaining)))
                    '()))))]
       (drain PersistentQueue/EMPTY chunks)))))
