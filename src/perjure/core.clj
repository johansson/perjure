(ns perjure.core
	(:gen-class :main true)
	(:use clojure.java.io)
	(:require [markdown.core :as md])
	(:require [clojure.string :as string]))

; forward declarations for template.clj dictionaries
(declare strings)
(declare files)
(declare blog-post-keys)
(declare blog-posts-keys)

(defn isWindows? [] (.contains (System/getProperty "os.name") "Windows"))

; do they need to be functions or can they be defined as is
(defn pathSep [] (if isWindows? "\\" "/"))
(defn pathSepR [] (if isWindows? #"\\" #"/"))

(defn apply-template
	"Apply a dictionary (in template.clj or generated), replacing all
	instances of keys with values in the string template and returns
	a new string with the results."
	[hmap template]
	(reduce
		(fn [acc m] (apply string/replace acc m))
		template
		(map vector (keys hmap) (vals hmap))
	)
)

(defn get-title-and-summary-for-post
	"Return a list with title and summary from a Markdown entry."
	[post]
	(binding [*in* (java.io.BufferedReader. (java.io.FileReader. post))]
		(list (clojure.string/trim (clojure.string/replace (read-line) #"#" "")) (md/md-to-html-string (read-line)))
	)
)

(defn get-posts
	"Returns a list of files pointing to the Markdown files in the directory given."
	[dir]
	(filter
		(fn [file] (.endsWith (.toLowerCase (.getAbsolutePath file)) ".md"))
		(file-seq (clojure.java.io/file dir))
	)
)

(defn html-file-for
	"Return the string renaming the .md file to .html."
	[file]
	(str (first (string/split (last (string/split (.getAbsolutePath file) (pathSepR))) #"\.")) ".html")
)

(defn generate-post
	"Generate a post from the Markdown file given."
	[src dst file]
	(def html-file (html-file-for file))
	(println (str "Generating " html-file))
	(def title (first (get-title-and-summary-for-post file)))
	(load-file (str src (pathSep) "template.clj"))
	(let [[year month day] (string/split html-file #"-")]
		(with-open [wrtr (writer (str dst (pathSep) html-file))]
			(.write wrtr
				(apply-template
					strings
					(apply-template
						(hash-map
							"{content}"
							(apply-template 
								(apply hash-map
									(interleave blog-post-keys
										(list
											html-file															; url
											title																; title
											day																	; day
											month																; month
											year																; year
											(md/md-to-html-string (slurp file))									; blog post
										)
									)
								)
								(slurp (str src (pathSep) (get files "{blog-post}")))
							)
						)
						(slurp (str src (pathSep) "template.html"))
					)
				)
			)
		)
	)
)

(defn generate-index
	"Generate the index page for the blog."
	[src dst posts]
	(println "Generating index.html")
	(load-file (str src (pathSep) "template.clj"))
	(with-open [wrtr (writer (str dst (pathSep) "index.html"))]
		(.write wrtr
			(apply-template
				strings
				(apply-template
					(hash-map
						"{content}"
						(apply
							str
							(map
								(fn [file]
									(let [[year month day] (string/split (html-file-for file) #"-") [title summary] (get-title-and-summary-for-post file)]
										(apply-template 
											(apply
												hash-map
												(interleave blog-posts-keys
													(list
														(html-file-for file)															; url
														title											; title
														day																	; day
														month																; month
														year																; year
														(md/md-to-html-string summary)					; blog post summary
													)
												)
											)
											(slurp (str src (pathSep) (get files "{blog-posts}")))
										)
									)
								)
								posts
							)
						)
					)
					(slurp (str src (pathSep) "template.html"))
				)
			)
		)
	)
)

(defn -main
	"Perjure - a blogging engine written in Clojure"
	[& args]
	(println "Perjure 0.1")
	(let [[loc src dst] args]							; loc = location of .md files
		(def posts (get-posts loc))						; src = location of template files
		(try											; dst = output directory
			(doall
				(concat
					(map (partial generate-post src dst) posts)
					(list (generate-index src dst posts))
				)
			)
			(catch Exception e (binding [*out* *err*] (println (str "error: " (.getMessage e))) "failure."))
			(finally (println "Exiting Perjure"))
		)
	)
)
