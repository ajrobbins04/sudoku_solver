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

(peers [:a1])
    
; =============================
; Define the Playing Grid
; =============================

(def grid-chars "003020600900305001001806400008102900700000008006708200002609500800203009005010300")

;; create a new vector by converting each 
;; character in grid-chars to a string, then an int
(defn create-grid-vector [grid-chars]
  (vec (map #(Integer/parseInt (str %)) grid-chars)))

;; initial vector containing grid values
(def grid (create-grid-vector grid-chars))

;; predicate method to check if val in grid
;; is within the inclusive range of 1-9
(defn is-valid-value? [val]
  (and (>= val 1) (<= val 9)))

;; a map is formed by pairing every square with a value from 
;; the grid vector based on matching order.
(def grid-values 
  (zipmap squares (map #(if (is-valid-value? %) % nil) grid)))

;; checks if the square is initially filled w/a value.
;; a predicate function for parse-grid.
(defn is-filled? [square] 
  (let [value (get grid-values square)] (not (= value nil))))

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


;; Converts grid squares into a map as the key w/all
;; possible digit values as its corresponding value.
(defn parse-grid []
  (into {} (for [square squares]
             (let [value (get grid-values square)]
               (if (is-filled? square) 
                 [square #{value}]     ;; assign digits 1-9 to empty squares
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

;; checks if a value is in the possible values 
(defn in-poss-values? [poss-values val]
  (if (some #(= val %) poss-values) true false))


(def poss {[:e9] #{8},
 [:d7] #{9},
 [:c2] #{7 1 4 6 3 2 9 5 8},
 [:h7] #{7 1 4 6 3 2 9 5 8},
 [:d8] #{7 1 4 6 3 2 9 5 8},
 [:g5] #{7 1 4 6 3 2 9 5 8},
 [:d6] #{2},
 [:e6] #{7 1 4 6 3 2 9 5 8},
 [:b3] #{7 1 4 6 3 2 9 5 8},
 [:h9] #{9},
 [:i5] #{1},
 [:e4] #{7 1 4 6 3 2 9 5 8},
 [:g4] #{6 5 3 2 1},
 [:a7] #{6 3},
 [:a3] #{3 2},
 [:a1] #{7 1 4 6 3 2 9 5 8},
 [:h1] #{8}})


(def peers {[:a7]
 [[:e4]
  [:g4]
  [:a3]
  [:a1]],
 [:d7]
 [[:d1]
  [:d2]
  [:d3]
  [:d4]
  [:d5]
  [:d6]
  [:d8]
  [:d9]
  [:a7]
  [:b7]
  [:c7]
  [:e7]
  [:f7]
  [:g7]
  [:h7]
  [:i7]
  [:d8]
  [:d9]
  [:e7]
  [:e8]
  [:e9]
  [:f7]
  [:f8]
  [:f9]]})

  
(defn in-peer-pv [])
(defn eliminate [poss-values square val]
  (cond 
    ;; return possible values map when val isn't in the possible values for square
    (not (in-poss-values? (poss-values square) val)) poss-values     
    ;; return possible values map when val is the only possible value for square
    (= #{val} (poss-values square)) poss-values
    ;; val can be removed from the possible-values for square
    :else (let [poss-values (assoc-in poss-values [square] (disj (poss-values square) val))] poss-values
               (if (= #{val} (poss-values square))
                 (reduce-true #(eliminate poss-values square (first (poss-values square))) poss-values (peers square)) )poss-values)))

(eliminate poss [:a7] 3)
; ===================================
; Utility Functions
; ===================================

(defn assign [poss-values square val] 
  ;; eliminate is applied w/in reduce-true
  (reduce-true (eliminate poss-values square val) poss-values (disj (poss-values square) val))) ;; reduce-true 

