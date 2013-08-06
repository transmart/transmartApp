Commit Messages
===============

1. Commit messages should be reasonably informative. They should describe _what_
   was changed (which area/subsystems of the code, not the files themselves, as
   that information is already displayed by git), _in which way_ they were
   changed and the _rationale_ behind the change. Any known bugs or limitations
   should be described, as well which other subsystems are impacted, if any.
2. If there's an associated ticket, it should be mentioned.
3. Formal requirements of commit messages are explained in [this web
   page][gitcommi].  To know: the first line should have not more than 50
   characters (if possible), the following one should be empty and the other
   lines should be wrapped at 72 characters.  Long text that should not be
   wrapped (like long URLs) can exceed 72 characters.


Code Conventions
================

Please follow these conventions when submitting new code:

1. Java code should follow the [_Code Conventions for the Java Programming
   Language_][javaconv].
2. Groovy code should follow the Java conventions mentioned in 1., modified by
   the [Groovy guidelines][groovygu].
3. The Java conventions define the unit of indentation as 4 spaces, but leave
   unspecified whether tabs or spaces should be used. For this project, _spaces_
   should be used for indentation. The rationale is the same mentioned
   [here][psr2-2.4].
4. Unix line endings should be used. The working directory can use Windows line
   endings, as long as these are not committed. New projects should enforce this
   with a [.gitattributes file][gitattri]; since this would involve mass
   converting all files in transmartApp and thus polluting the history, the
   conversion should be done on a per-file basis, when it's first touched.
   _Windows machines_ should be properly configured so that they commit LF line
   endings. You can change the value of Git's _core.eol_ and _core.autocrlf_ so
   that the files are checked out with CRLF line endings, if you prefer, but
   make sure that the combination you choose does not result in CRLF line
   endings being committed, especially in files that were already using LF line
   endings.
5. An empty line should always be included at the end of the file.
6. Do not include trailing whitespace, including on blank lines.  7.Aim for 80
   character margins. In highly indented code (which should nevertheless be
   avoided), try not to exceed 120 characters per line.
8. Remember to use 8 space continuations (not 4 spaces), as described in the
   Java code conventions.
9. If you are going to fix the conventions on a file and also change it
   substantially, do it in two separate commits so that the substantial changes
   can be reviewed more easily.


An IntelliJ code style file is [available][cocofile]. This is a
non-authoritative work-in-progress. Copy it to
~/.IdeaDirectory/config/codestyles and restart transmart. You should now find
the code style transmart under Settings &gt; Code style. You should also go to
Settings &gt; Editor, change _Strip trailing spaces on Save_ to _Modified Lines_
and check _Ensure line feed at file end on Save_.

  [gitcommi]: http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html
  [javaconv]: http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html
  [groovygu]: http://groovy.codehaus.org/Groovy+style+and+language+feature+guidelines+for+Java+developers
  [psr2-2.4]: https://github.com/php-fig/fig-standards/blob/master/accepted/PSR-2-coding-style-guide.md#24-indenting
  [gitattri]: https://www.kernel.org/pub/software/scm/git/docs/gitattributes.html#_end_of_line_conversion
  [cocofile]: http://files.thehyve.net/transmart_cs.xml
