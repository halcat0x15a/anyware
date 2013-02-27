(ns anyware.core.lisp.environment)

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
