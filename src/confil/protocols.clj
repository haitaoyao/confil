(ns confil.protocols)

(defprotocol Configure

  (-config [path-or-obj] [path-or-obj ConfigImplementer]
          "Docs"))

