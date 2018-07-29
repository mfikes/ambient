(ns main.core
  (:require-macros
   [main.core :refer [analyzer-state]])
  (:require
   [cljs.js]
   [library.core]))

(def state (cljs.js/empty-state))

(defn evaluate [source cb]
  (cljs.js/eval-str state source nil {:eval cljs.js/js-eval :context :expr} cb))

(def library-analysis-cache (analyzer-state 'library.core))

(defn load-library-analysis-cache! []
  (cljs.js/load-analysis-cache! state 'library.core library-analysis-cache)
  nil)
