
(use 'clojure.contrib.duck-streams)

(def file-counts
  (map
    #(list (.toString %) (count (read-lines %)))
    (remove #(.isDirectory %)
      (file-seq (file-str "~/projects/FitNesseGit/src/fitnesse")))))

(defn count-lines-that-end-with [file-counts suffix]
  (reduce +
    (map second
      (filter #(.endsWith (first %) suffix) file-counts))))

(def java-count (count-lines-that-end-with file-counts ".java"))
(def test-count (count-lines-that-end-with file-counts "Test.java"))

(printf "Java lines: %d\n" java-count)
(printf "Test lines: %d\n" test-count)
(printf "Test pct: %.1f\n" (double (* 100 (/ test-count java-count))))
