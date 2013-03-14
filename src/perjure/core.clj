(ns perjure.core
	(:gen-class :main true)
	(:require [clojure.java.io :as cljio])
	(:require [clojure.string :as string])
	(:require [markdown.core :as md]))

; forward declarations for template.clj dictionaries
(declare strings)
(declare files)
(declare blog-post-keys)	; for blog page
(declare blog-posts-keys) 	; for index page

(defn isWindows? [] (.contains (System/getProperty "os.name") "Windows"))

; do they need to be functions or can they be defined as is
(defn pathSep [] (if isWindows? "\\" "/"))
(defn pathSepR [] (if isWindows? #"\\" #"/"))

(defn err-println [msg]
	(binding [*out* *err*] (println msg)))

; safe
(defn get-ymd [filename]
	(take 3 (string/split filename #"-")))
	
; safe
(defn html-filename-for
	"Return the string renaming the .md file to .html."
	[filename ps]
	(let [	[year month day] (get-ymd filename)
			md-filename (string/replace filename (str year "-" month "-" day "-") "")
			bare-filename (string/replace md-filename #"\.[Mm][Dd]$" "")]
		(str ps year ps month ps day ps bare-filename ".html")))

; unsafe
(defn spit-dict-to-html
	[hmap]
	(doseq [[path content] hmap]
		(try
			(let [file (cljio/file path)
				 parent (.getParentFile file)]
				(if (.mkdirs parent)
					(spit file content)
					(err-println (str "error: could not make parent dir: " (.getAbsolutePath parent)))))
			(catch SecurityException e
				(err-println (str "security error: " (.getMessage e))))
			(catch java.io.IOException e
				(err-println (str "i/o error: " (.getMessage e)))))))

; unsafe
(defn parse-post
	"Parse the filename, contents, etc. for a Post."
	[post]
	(let [raw-file (slurp post) [year month day] (get-ymd (.getName post))]
		(with-open [reader (java.io.BufferedReader. (java.io.StringReader. raw-file))]
			(let [[title summary] (filter #(not (string/blank? %)) (line-seq reader))]
				(list
					(string/trim (string/replace title #"#" ""))
					year
					month
					day
					(md/md-to-html-string summary)
					(md/md-to-html-string raw-file)
					(html-filename-for (.getName post) "/"))))))

; unsafe
(defn get-posts
	"Returns a list of files pointing to the Markdown files in the directory given."
	[dir]
	(filter
		#(.matches (re-matcher #"^\d{4}-\d{2}-\d{2}-.+?\.[Mm][Dd]$" (.getName %)))
		(file-seq (cljio/file dir))))

; safe
(defn apply-template
	"Apply a dictionary (in template.clj or generated), replacing all
	instances of keys with values in the string template and returns
	a new string with the results."
	[hmap template]
	(reduce
		(fn [acc m] (apply string/replace acc m))
		template
		(map vector (keys hmap) (vals hmap))))
			
; safe
(defn generate-page
	[page-template strings title virgin]
	(string/replace 
		(apply-template	strings
			(apply-template (hash-map "{content}" virgin) page-template))
		"{page-title}"
		title))

; safe
(defn generate-post
	"Generate a post from the Markdown file given."
	[page-template post-template strings k post]
	(let 	[[title year month day summary post url] post]
		(generate-page page-template strings title
			(apply-template 
				(apply
					hash-map
					(interleave k (list url title day month year post)))
				post-template))))

; safe
(defn generate-summary-for-index
	[post-template k url title day month year summary]
	(apply-template 
		(apply
			hash-map
			(interleave k
				(list url title day month year summary)))
		post-template))

; safe
(defn generate-index
	"Generate the index page for the blog."
	[page-template post-template strings k posts]
	(generate-page page-template strings "Index"
		(apply str
			(map
				#(let [[title year month day summary] (get posts %)]
					(generate-summary-for-index post-template k % title day month year summary))
				(keys posts)))))

(defn -main
	"Perjure - a blogging engine written in Clojure"
	[& args]
	(println "Perjure 0.2-beta")
	(load-file (str (second args) (pathSep) "template.clj"))
	(let	[[loc src dst] args ; loc = location of .md files ; src = location of template files ; dst = output directory
			page-template 		(slurp (str src (pathSep) (get files "page-template")))
			blog-post-template 	(slurp (str src (pathSep) (get files "blog-post")))
			blog-posts-template	(slurp (str src (pathSep) (get files "blog-posts")))
			posts (get-posts loc)
			parsed-posts (map parse-post posts)
			paths-for-posts (map #(str dst (html-filename-for (.getName %) (pathSep))) posts)
			urls-for-posts (map #(html-filename-for (.getName %) "/") posts)]
		(try
			(spit-dict-to-html
				(let [generated-post (map (partial generate-post page-template blog-post-template strings blog-post-keys) parsed-posts)]
					(apply hash-map (interleave paths-for-posts generated-post))))
			(spit
				(str dst (pathSep) "index.html")
				(generate-index page-template blog-posts-template strings blog-posts-keys (apply hash-map (interleave urls-for-posts parsed-posts))))
		(catch Exception e (.printStackTrace e) -1)
		(finally (println "Exiting Perjure")))))
