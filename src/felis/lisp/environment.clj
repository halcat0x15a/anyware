(ns felis.lisp.environment)

(def path [:root :environment])

(defn add [editor key function]
  (update-in editor path #(assoc % key function)))

(def global
  {(symbol "nil") nil
   (symbol "true") true
   (symbol "false") false
   '+ +
   '- -
   '* *
   '= =
   '<= <=
   'mod mod
   'dec dec
   'not not
   'zero? zero?
   'nil? nil?
   'atom atom
   'deref deref
   'reset! reset!
   'number? number?
   'string? string?
   'symbol? symbol?
   'seq? seq?
   'str str
   'symbol symbol
   'list list
   'cons cons
   'empty? empty?
   'first first
   'rest rest
   'next next
   'ffirst ffirst
   'fnext fnext
   'nnext nnext
   'reverse reverse
   'count count
   'prn prn})
