# perjure

A static blog compiler written in Clojure.

## Usage

> `java -jar perjure-0.1.0-SNAPSHOT-standalone.jar /path/to/posts /path/to/templates /path/to/output`

## Blog structure

Your posts directory should only have markdown files,
and their names should be formatted as:

> `YYYY-mm-dd-anything.md`

Be sure the file name ends with the extension ".md",
otherwise `perjure` won't be able to find them and
generate anything for you!

Your template directory shall contain a Clojure file, named
`template.clj`. This contains the dictionaries for the
templated variables. You mad add any variable to strings
in the format of `{variable-name}` and map it to a Clojure
function that returns a string, and put it anywhere in the
HTML template files. However, the blog post and blog posts keys
must not be modified, unless you want to extend `perjure`.

Your template directory shall also contain three HTML files.
By default they are:

- `template.html`, which is the full page template.
- `blog-posts.html`, which is the index page template.
- `blog-post.html`, which is the blog post page template.

`template.html` is required. The others may be named whatever
you like, but you must remap it in `template.clj`, under the
files dictionary.

## Blog entry formatting

`perjure` is unfortunately a bit inflexible about the markdown
files for a reason. `perjure` generates the blog entry title
and summary from the first two lines. The first line is the
title. The second line is the summary. I plan to change this
and make it more flexible. This is a first release, after all. :)

Also, I don't generate the directory structure right now. That's
for 0.1.1. Sorry. :(

## License

Copyright © 2013 Will Johansson

Distributed under the [LGPLv3](http://www.gnu.org/licenses/lgpl-3.0.txt).
