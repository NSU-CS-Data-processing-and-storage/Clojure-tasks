(ns tasks.c4-test
  (:require [clojure.test :refer :all]
            [tasks.c4 :as c4]))

;; Простой тест: проверяем, что supply-msg увеличивает счётчик на складе
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
        ;; на цикл нужно 2 единицы Ore
        fac   (c4/factory 1 0 prod "Ore" 2)]
    ;; кладём только 1 единицу руды
    (swap! (:storage ore) + 1)
    ;; уведомляем фабрику, что 1 единица Ore появилась
    (send (:worker fac) c4/notify-msg "Ore" (:storage ore) 1)
    (await (:worker fac) (:worker prod))
    ;; продукта быть не должно
    (is (= 0 @(:storage prod)))
    ;; руда осталась, т.к. валидатор не дал списать больше, чем есть
    (is (= 1 @(:storage ore)))))

;; Если входного ресурса достаточно, notify-msg должен запустить производство
(deftest notify-msg-enough-input-produces
  (let [ore   (c4/storage "Ore" 0)
        prod  (c4/storage "Product" 0)
        ;; на цикл нужно 2 единицы Ore
        fac   (c4/factory 1 0 prod "Ore" 2)]
    ;; кладём 2 единицы руды
    (swap! (:storage ore) + 2)
    ;; уведомляем фабрику о приходе ресурса
    (send (:worker fac) c4/notify-msg "Ore" (:storage ore) 2)
    (await (:worker fac) (:worker prod))
    ;; произведён ровно 1 продукт
    (is (= 1 @(:storage prod)))
    ;; вся руда была израсходована
    (is (= 0 @(:storage ore)))))
