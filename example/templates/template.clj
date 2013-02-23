; Templating engine requires these keys (and values) to operate
(ns perjure.core)

(def strings
	(hash-map
		"{site-home}" "http://tempuri.org",	; change to your URL
		"{site-title}" "Temporary Site",	; what do you want to call your site?
		"{blog-title}" "Blog"				; what do you want to call your blog?
		"{blog-home}" "blog"				; where does blog reside? if at root, use ""
		"{perjure-version}" "0.1"
		"{github-account}" "your-github-account")
)

(def files
	(hash-map
		"{blog-post}" "blog-post.html",		; point it to your blog post template
		"{blog-posts}" "blog-posts.html"))	; point it to your index of blog posts template

; Do not edit below this.
(def blog-post-keys
	(list
		"{current-post-url}"
		"{current-post-title}"
		"{current-post-day}"
		"{current-post-month}"
		"{current-post-year}"
		"{current-post}"))
		
(def blog-posts-keys
	(list
		"{current-post-url}"
		"{current-post-title}"
		"{current-post-day}"
		"{current-post-month}"
		"{current-post-year}"
		"{current-post-summary}"))
		
; (get files "blog-posts")
; (apply hash-map (interleave blojure.template/blog-posts-keys (list actual content)))