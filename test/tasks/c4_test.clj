(ns tasks.c4-test
  (:require [clojure.test :refer :all]
            [tasks.c4 :as c4]))

;; Проверяем, что supply-msg увеличивает счётчик на складе
(deftest storage-supply-increases-counter
  (let [st (c4/storage "Test" 0)
        w  (:worker st)]
    (send w c4/supply-msg 5)
    (await w)
    (is (= 5 @(:storage st)))))

;; Если входного ресурса достаточно, notify-msg должен запустить производство
(deftest notify-msg-enough-input-produces
  (let [ore   (c4/storage "Ore" 0)
        prod  (c4/storage "Product" 0)
        fac   (c4/factory 1 0 prod "Ore" 2)]
    (swap! (:storage ore) + 2)
    (send (:worker fac) c4/notify-msg "Ore" (:storage ore) 2)
    (await (:worker fac) (:worker prod))
    ;; произведён ровно 1 продукт
    (is (= 1 @(:storage prod)))
    ;; вся руда была израсходована
    (is (= 0 @(:storage ore)))))
