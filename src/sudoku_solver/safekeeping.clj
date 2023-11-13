;; specify project's namespace
(ns sudoku-solver.core)

; =============================
; Implement Global Variables 
; =============================

(def digits #{1 2 3 4 5 6 7 8 9})
(def grid "003020600900305001001806400008102900700000008006708200002609500800203009005010300")
;; create a new vector from grid1 by converting each character to a string, then an int
(def grid-vector (vec (map #(Integer/parseInt (str %)) grid)))

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

;; predicate method to check if val in grid
;; is within the inclusive range of 1-9
(defn is-valid-value? [val]
  (and (>= val 1) (<= val 9)))

;; a map is formed by pairing every square with a value from 
;; the grid vector based on matching order.
(def grid-values 
  (zipmap squares (map #(if (is-valid-value? %) % nil) grid-vector)))

;; checks if the square is initially filled w/a value.
;; a predicate function for parse-grid.
(defn is-filled? [square] 
  (let [value (get grid-values square)] (not (= value nil))))

;; returns initial values assigned to squares
(defn val? [square]
  (let [value (get grid-values square)] value))

;; Converts grid squares into a map as the key w/all
;; possible digit values as its corresponding value.
(def parsed-grid 
  (into {} (for [square squares]
             (let [value (get grid-values square)]
               (if (is-filled? square) 
                 [square value]        ;; assign digits 1-9 to empty squares
                 [square digits])))))  ;; assign actual value to filled squares


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

