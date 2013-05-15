# perjure

A static blog compiler written in Clojure.

## Usage

> `java -jar perjure-0.2.0-standalone.jar /path/to/posts /path/to/templates /path/to/output`

## Blog structure

Your posts directory should only have markdown files,
and their names should be formatted as:

> `YYYY-mm-dd-anything.md`

Be sure the file name ends with the extension ".md",
otherwise `perjure` won't be able to find them and
generate anything for you!

Your template directory shall contain a Clojure file, named
`template.clj`. This contains the dictionaries for the
templated variables. You may add any variable to strings
in the format of `{variable-name}` and map it to a Clojure
function that returns a string, and put it anywhere in the
HTML template files. However, the blog post and blog posts keys
must not be modified, unless you want to extend `perjure`.

Your template directory shall also contain three HTML files.
By default they are:

- `template.html`, which is the full page template.
- `blog-posts.html`, which is the index page template.
- `blog-post.html`, which is the blog post page template.

They may be named whatever you like, but you must remap it in
`template.clj`, under the files dictionary.

## Blog entry formatting

`perjure` is now more flexible about the extracting blog post
metadata from the Markdown files. Instead of first two lines
(blank or non-blank, which might have been a problem for some
users), it will read the file and get the first non-blank line
and use that for the title, then use the second non-blank line
for the summary.

Now it supports directory structuring. However, it's very strict
about that, so the only current supported directory structure is
`${BLOG-HOME}/YYYY/mm/dd/`. But the cool thing is that `perjure`
doesn't care where you put the Markdown files in the posts
directory you pass as an argument to it. It just cares about the
file names, so beware of duplicate file names! I designed this way
so the user can organize his blog as logically as he wants it.
Perhaps the user thinks it's best to structure it as:

> `/path/to/posts/computers`

> `/path/to/posts/family`

> `/path/to/posts/unlabeled/2012`

Whatever the reasoning, all that matters is that you follow the
basic rule of file naming. Then it does not matter where the .md
files are, as long as they are somewhere in the posts directory.

## License

Copyright © 2013 Will Johansson

Distributed under the [LGPLv3](http://www.gnu.org/licenses/lgpl-3.0.txt).
