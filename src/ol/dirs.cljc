(ns ol.dirs
  "Cross-platform directory lookup for Clojure, ClojureScript on Node, and ClojureDart.

  The public API returns strings or vectors of strings and never creates directories.

  - arity 0 returns the base directory
  - arity 1 appends an application name
  - arity 2 appends qualifier, organization, and application using platform rules

  See [[config-home]], [[data-home]], [[state-home]], and [[home-dir]]."
  (:require [ol.dirs.impl :as impl]
            [ol.dirs.runtime.current :as current]))

(defn data-home
  "Returns the writable user data directory."
  ([] (impl/resolve-directory (current/current-context) :data-home))
  ([application] (impl/resolve-directory (current/current-context) :data-home application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :data-home qualifier organization application)))

(defn config-home
  "Returns the writable user configuration directory."
  ([] (impl/resolve-directory (current/current-context) :config-home))
  ([application] (impl/resolve-directory (current/current-context) :config-home application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :config-home qualifier organization application)))

(defn state-home
  "Returns the writable user state directory."
  ([] (impl/resolve-directory (current/current-context) :state-home))
  ([application] (impl/resolve-directory (current/current-context) :state-home application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :state-home qualifier organization application)))

(defn data-dirs
  "Returns the shared data search roots as a vector of strings."
  ([] (impl/resolve-directory (current/current-context) :data-dirs))
  ([application] (impl/resolve-directory (current/current-context) :data-dirs application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :data-dirs qualifier organization application)))

(defn config-dirs
  "Returns the shared configuration search roots as a vector of strings."
  ([] (impl/resolve-directory (current/current-context) :config-dirs))
  ([application] (impl/resolve-directory (current/current-context) :config-dirs application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :config-dirs qualifier organization application)))

(defn cache-home
  "Returns the writable user cache directory."
  ([] (impl/resolve-directory (current/current-context) :cache-home))
  ([application] (impl/resolve-directory (current/current-context) :cache-home application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :cache-home qualifier organization application)))

(defn runtime-dir
  "Returns the runtime directory, or `nil` on platforms without one."
  ([] (impl/resolve-directory (current/current-context) :runtime-dir))
  ([application] (impl/resolve-directory (current/current-context) :runtime-dir application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :runtime-dir qualifier organization application)))

(defn executable-dir
  "Returns the user executable directory, or `nil` when unsupported."
  ([] (impl/resolve-directory (current/current-context) :executable-dir))
  ([application] (impl/resolve-directory (current/current-context) :executable-dir application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :executable-dir qualifier organization application)))

(defn preference-dir
  "Returns the preference directory."
  ([] (impl/resolve-directory (current/current-context) :preference-dir))
  ([application] (impl/resolve-directory (current/current-context) :preference-dir application))
  ([qualifier organization application]
   (impl/resolve-directory (current/current-context) :preference-dir qualifier organization application)))

(defn home-dir
  "Returns the user's home directory."
  [] (impl/resolve-directory (current/current-context) :home-dir))

(defn audio-dir
  "Returns the user's audio or music directory, or `nil` when unresolved."
  [] (impl/resolve-directory (current/current-context) :audio-dir))

(defn desktop-dir
  "Returns the user's desktop directory, or `nil` when unresolved."
  [] (impl/resolve-directory (current/current-context) :desktop-dir))

(defn document-dir
  "Returns the user's documents directory, or `nil` when unresolved."
  [] (impl/resolve-directory (current/current-context) :document-dir))

(defn download-dir
  "Returns the user's downloads directory, or `nil` when unresolved."
  [] (impl/resolve-directory (current/current-context) :download-dir))

(defn font-dir
  "Returns the user's font directory, or `nil` when unsupported."
  [] (impl/resolve-directory (current/current-context) :font-dir))

(defn picture-dir
  "Returns the user's pictures directory, or `nil` when unresolved."
  [] (impl/resolve-directory (current/current-context) :picture-dir))

(defn public-dir
  "Returns the user's public sharing directory, or `nil` when unresolved."
  [] (impl/resolve-directory (current/current-context) :public-dir))

(defn template-dir
  "Returns the user's templates directory, or `nil` when unsupported or unresolved."
  [] (impl/resolve-directory (current/current-context) :template-dir))

(defn video-dir
  "Returns the user's videos directory, or `nil` when unresolved."
  [] (impl/resolve-directory (current/current-context) :video-dir))

(defn state-dir
  "Alias of [[state-home]]."
  ([] (state-home))
  ([application] (state-home application))
  ([qualifier organization application]
   (state-home qualifier organization application)))

(defn config-dir
  "Alias of [[config-home]]."
  ([] (config-home))
  ([application] (config-home application))
  ([qualifier organization application]
   (config-home qualifier organization application)))
