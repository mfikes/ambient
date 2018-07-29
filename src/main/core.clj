(ns main.core
  (:require
   [cljs.env :as env]))

(defmacro analyzer-state [[_ ns-sym]]
  (let [state (get-in @env/*compiler* [:cljs.analyzer/namespaces ns-sym])]
    `'~state))
