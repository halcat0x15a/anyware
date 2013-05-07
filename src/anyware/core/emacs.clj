(ns anyware.core.emacs)

(def keymap
  {#{:ctrl \f} api/right
   #{:ctrl \b} api/left
   #{:ctrl \n} api/down
   #{:ctrl \p} api/up
   #{:alt \f} api/right-word
   #{:alt \b} api/left-word
   #{:ctrl \e} api/end-of-line
   #{:ctrl \a} api/beginning-of-line
   #{:ctrl \m} api/break
   #{:ctrl \h} api/backspace
   #{:ctrl \d} api/delete)
