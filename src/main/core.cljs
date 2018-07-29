(ns main.core
  (:require-macros
   [main.core :refer [analyzer-state]])
  (:require
   [cljs.js]
   [library.core]))

(def state (cljs.js/empty-state))

(defn evaluate [source cb]
  (cljs.js/eval-str state source nil {:eval cljs.js/js-eval :context :expr} cb))

(defn load-library-analysis-cache! []
  (cljs.js/load-analysis-cache! state 'library.core (analyzer-state 'library.core))
  nil)
