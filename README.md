Port of AstroImageJ to Java 8
=============================

All changes for the Java 8 port are in the branch ai8.

Preserve master for updates from upstream, and periodically rebase ai8 onto them.

This project's maven build is not intended to work standalone.

Instead, this project is buildable as a submodule of the wrapper project
https://github.com/observatree/aij. The two submodules of the wrapper project
need to be versioned and built together.

This could eventually be remedied by making an installable artifact
in the ai8 branch of https://github.com/observatree/imagej-ImageJA, but
at present the projects are too tightly coupled to make that worthwhile.
