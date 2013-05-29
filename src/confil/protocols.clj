(ns confil.protocols)

(defprotocol Configure

  (-config [path-or-obj] [path-or-obj ConfigImplementer]
          "Create a typesafe Config from a given source.
          Optionally cast the Config up to a more specific implementation,
          given a concrete class, ConfigImplementer."))

