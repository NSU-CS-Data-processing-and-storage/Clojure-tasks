(ns test-runner
  (:require
   [clojure.test :as t]
   [tasks.c1-test]
   [tasks.c2-test]
   [tasks.c3-test])
  (:gen-class))

(defn -main [& _]
  (let [res (t/run-tests 'tasks.c1-test 'tasks.c2-test 'tasks.c3-test)
        exit-code (if (pos? (+ (:fail res 0) (:error res 0))) 1 0)]
    (shutdown-agents)
    (System/exit exit-code)))
