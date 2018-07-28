(ns main.core)

(defmacro slurp-edn [file]
  `'~(read-string (slurp file)))
