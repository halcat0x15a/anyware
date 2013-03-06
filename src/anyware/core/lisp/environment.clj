(ns anyware.core.lisp.environment
  (:refer-clojure :exclude [compare test map]))

(def literal
  {(symbol "nil") nil
   (symbol "true") true
   (symbol "false") false})

(def arithmetic
  {'+ +
   '- -
   '* *
   '/ /
   'mod mod
   'inc inc
   'dec dec
   'not not})

(def compare
  {'= =
   '< <
   '> >
   '<= <=
   '>= >=})

(def test
  {'nil? nil?
   'number? number?
   'string? string?
   'symbol? symbol?
   'seq? seq?})

(def reference
  {'atom atom
   'deref deref
   'reset! reset!})

(def collection 
  {'list list
   'cons cons
   'first first
   'next next
   'ffirst ffirst
   'fnext fnext
   'nnext nnext
   'empty? empty?
   'reverse reverse
   'count count})

(def create
  {'str str
   'symbol symbol})

(def io
  {'prn prn})

(def global
  (merge
   literal
   arithmetic
   compare
   test
   reference
   collection
   create
   io))
