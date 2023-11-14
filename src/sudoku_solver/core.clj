;; specify project's namespace
(ns sudoku-solver.core)


; =============================
; Implement Global Variables 
; =============================

;; set used to avoid duplicate values
(def digits #{1 2 3 4 5 6 7 8 9})

(def rows [\a \b \c \d \e \f \g \h \i])
(def cols [1 2 3 4 5 6 7 8 9])

;; Contains every possible sudoku board location as
;; a keyword for more efficient map lookup.
(def squares (for [row rows col cols] [(keyword (str row col))]))

;; Every unit that's a collection of 9 squares
;; and arranged in a row
(def unit-rows 
  (for [row rows]  ;; binds every element of rows to row
    (for [col cols] [(keyword (str row col))])))

;; Every unit that's a collection of 9 squares
;; and arranged in a column
(def unit-cols 
  (for [col cols]  ;; binds every element of cols to col
    (for [row rows] [(keyword (str row col))])))

;; Every unit that's a collection of 9 squares
;; and arranged in a 3x3 square
(def unit-squares
  (for [three-rows (partition 3 rows) three-cols (partition 3 cols)]
                        (for [row three-rows col three-cols] [(keyword (str row col))])))

;; Contains every possible unit grouped by arrangement type
(def all-units
  (concat unit-rows unit-cols unit-squares))

;; the predicate function of units-by-square
(defn in-unit? [square units]
  (some #{square} units))

;; The key is a square on the board & its value 
;; is all 3 units that the square belongs to
(def units-by-key
  (into {} (for [square squares]
             [square (for [units all-units
                           :when (in-unit? square units)] units)])))

;; first method used to define peers
(defn combine-nested-units [units-by-key]
  (into {} (for [[key units] units-by-key]
             [key (apply concat units)])))

;; second method used to define peers
(defn remove-key-from-value [units]
  (into {} (for [[key squares] units]
             [key (vec (remove (fn [sq] (= sq key)) squares))])))

;; Peers include every square that shares a unit with the square
;; representing the key
(def peers
  (let [combined-units (combine-nested-units units-by-key)]
    (remove-key-from-value combined-units)))


; =============================
; Define the Playing Grid
; =============================

(def grid-chars "003020600900305001001806400008102900700000008006708200002609500800203009005010300")

;; check if val is w/in the inclusive range of 1-9
(defn is-valid-value? [val]
  (and (>= val 1) (<= val 9)))

;; create a vector for the grid that holds integers
(defn create-int-grid [grid-chars]
  (vec (map #(Integer/parseInt (str %)) grid-chars)))

;; a map is formed by pairing every square with its
;; assigned value 
(defn add-initial-values [grid-chars]
  (zipmap squares (map #(if (is-valid-value? %) % nil) (create-int-grid grid-chars))))

;; Renders a map of squares w/all their possible values
(defn parse-grid [grid]
  (let [grid-values (add-initial-values grid)]
    (into {} (for [square squares]
             (let [value (grid-values square)]
               (if (nil? value) 
                 [square digits]         ;; assign digits 1-9 for empty squares
                 [square #{value}])))))) ;; assign given value for filled squares
                 
(parse-grid grid-chars)
; ==============================================
;
; Constraint Propagation 
;
; Rule 1: if a square has one possible value, 
;         then remove that value from its peers.
;
; Rule 2: if a unit has only one possible place  
;         for a value, then put the value there.
;
; ==============================================

;; checks if a value is in the possible values 
(defn in-poss-values? [poss-values val]
  (if (some #(= val %) poss-values) 
    true 
    false))

(declare assign)
(declare reduce-true)

(defn eliminate [poss-values square val]
  (let [square-poss-values (poss-values square)]
    (cond
      ;; Value is not found in possible values
      (not (in-poss-values? square-poss-values val))
      poss-values

      ;; Value is already the only possible value
      (= #{val} square-poss-values)
      ;; 
      (reduce-true (fn [values s] (eliminate values s val)) poss-values (peers square))

      :else
      (let [new-poss-values (assoc-in poss-values [square] (disj square-poss-values val))]
        (if (= 1 (count (new-poss-values square)))
          (reduce-true (fn [values s] (eliminate values s val)) new-poss-values (peers square))
          new-poss-values)))))

(defn extract-first-val [poss-values square-keyword]
  (let [entry (find poss-values [square-keyword])]
    (when entry
      (-> entry second first))))


;; every square will only be associated
;; w/one value once solved
(defn solved? [poss-values]
  (every? #(= 1 (count (poss-values %))) squares))

;; sort squares w/poss values by size of 
;; possible values in ascending order
(defn asc-sort [poss-values]
  (sort-by (comp count val) poss-values))

(defn search [poss-values]
  (if (solved? poss-values)  ;; check if puzzle already solved 
    poss-values              ;; return solution if solved
    (let [asc-poss-values (asc-sort poss-values)]
      (reduce-true (fn [values square] 
                     (let [square-poss-values (values square)]
                       (println "Processing square:" square)
                       (println "Possible values:" square-poss-values)
                       (eliminate poss-values square (first (square-poss-values)) ))
                     ) poss-values (map first asc-poss-values))asc-poss-values)))

(defn search [poss-values]
  (if (solved? poss-values)  ;; check if puzzle already solved 
    poss-values              ;; return solution if solved
    (let [asc-poss-values (asc-sort poss-values)]
      (let [asc-squares (map first asc-poss-values)]
      (reduce-true (fn [values square] 
                     (let [val (extract-first-val values square)]
                       (println "Processing square:" square)
                       (println "Possible values:" (poss-values square))
                       (println "Value:" val)
                       (eliminate poss-values square val ))
                     )poss-values asc-squares)) asc-poss-values)))


; ===================================
; Utility Functions
; ===================================

;; whittles down possible values from a collection
;; until a logical false is encountered
(defn reduce-true
  [func val collection]
  (when val   ;; check incoming val argument, is truthy 
    (loop [val val collection collection]   ;; bind the values from the arguments to the loop variables
      (if (empty? collection)   ;; if the collection is empty then return val
        val
        (when-let [val* (func val (first collection))] ;; when-let only binds the result to val if the result is truthy
          (recur val* (rest collection)))))))          ;; updated values will be used to re-enter the loop



(defn whittle-values [poss-values square val];; eliminate is applied w/in reduce-true
  (reduce-true #(eliminate %1 square val) poss-values (disj (poss-values square) val)))

(defn solve [grid] (-> grid parse-grid search))
(solve grid-chars)

(def poss {[:e9] #{8},
 [:d7] #{9},
 [:c2] #{7 1 4 6 3 2 5 8},
 [:h7] #{7 1 4 6 3 2 5 8},
 [:d8] #{7 1 4 6 3 2 5 8},
 [:g5] #{7 1 4 6 3 2 5 8},
 [:d6] #{2},
 [:e6] #{7 1 4 6 3 2 9 5 8},
 [:b3] #{7 1 4 6 3 2 9 5 8},
 [:h9] #{9},
 [:i5] #{1},
 [:e4] #{7 1 4 6 3 2 9 5 8},
 [:g4] #{6}})
(search poss)