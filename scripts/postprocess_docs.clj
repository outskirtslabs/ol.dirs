#!/usr/bin/env bb

(require '[babashka.fs :as fs]
         '[clojure.string :as str])

(def docs-root
  (fs/path "." "doc" "modules" "ROOT"))

(def api-pages-dir
  (fs/path docs-root "pages" "api"))

(def api-index-files
  [(fs/path docs-root "pages" "api.adoc")
   (fs/path docs-root "pages" "api" "index.adoc")])

(defn split-first-anchor
  [content]
  (if-let [idx (str/index-of content "\n[#")]
    [(subs content 0 (inc idx))
     (subs content (inc idx))]
    [content nil]))

(defn dedupe-page-entries
  [content]
  (let [[prefix entries-text] (split-first-anchor content)]
    (if-not entries-text
      content
      (let [entries (str/split entries-text #"\n\n'''\n\n(?=\[#)")
            seen (volatile! #{})
            keep-entry? (fn [entry]
                          (if-let [[_ anchor] (re-find #"\[#([^\]]+)\]" entry)]
                            (when-not (contains? @seen anchor)
                              (vswap! seen conj anchor)
                              true)
                            true))
            kept (filter keep-entry? entries)]
        (str prefix (str/join "\n\n'''\n\n" kept) "\n")))))

(defn dedupe-api-page!
  [file]
  (let [file (str file)
        content (slurp file)
        updated (dedupe-page-entries content)]
    (when (not= content updated)
      (spit file updated))))

(def row-pattern
  #"(?ms)\| xref:api/([^\[]+)\[`([^`]+)`\]\n\| (.+?)(?=\n\n\| xref:api/|\n\n\|===)")

(defn parse-index-rows
  [content]
  (map (fn [[_ href ns-name summary]]
         {:href href :ns-name ns-name :summary summary})
       (re-seq row-pattern content)))

(defn render-index-page
  [rows]
  (let [ns-count (count rows)
        body (->> rows
                  (map (fn [{:keys [href ns-name summary]}]
                         (str "| xref:api/" href "[`" ns-name "`]\n"
                              "| " summary)))
                  (str/join "\n\n"))]
    (str "= API Reference\n\n"
         "This release publishes " ns-count " namespace"
         (when (not= ns-count 1) "s")
         ".\n\n"
         "[cols=\"1,3\",options=\"header\",stripes=hover]\n"
         "|===\n"
         "| Namespace | Summary\n\n"
         body "\n\n"
         "|===\n")))

(defn dedupe-index!
  [file]
  (let [file (str file)
        rows (parse-index-rows (slurp file))
        deduped (vals (reduce (fn [acc row]
                                (update acc (:href row) #(or % row)))
                              {}
                              rows))]
    (spit file (render-index-page deduped))))

(doseq [file (->> (fs/glob api-pages-dir "*.adoc")
                  (remove #(= "index.adoc" (fs/file-name %))))]
  (dedupe-api-page! file))

(doseq [file api-index-files]
  (dedupe-index! file))
