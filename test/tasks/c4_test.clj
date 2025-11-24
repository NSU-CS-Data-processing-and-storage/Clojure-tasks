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

;; Если на складе входного ресурса мало, notify-msg не должен ничего производить
(deftest notify-msg-not-enough-input-no-product
  (let [ore   (c4/storage "Ore" 0)
        prod  (c4/storage "Product" 0)

        fac   (c4/factory 1 0 prod "Ore" 2)]
    (swap! (:storage ore) + 1)
    ;; уведомляем фабрику, что 1 единица Ore появилась
    (send (:worker fac) c4/notify-msg "Ore" (:storage ore) 1)
    (await (:worker fac) (:worker prod))
    ;; продукта быть не должно
    (is (= 0 @(:storage prod)))
    (is (= 1 @(:storage ore)))))

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
