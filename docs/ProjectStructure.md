Project Structure
=================

Information about the Quick Start
---------------------------------

The only warnings that should occur in the preceding steps
 are associated with `nom.tam.fits` library being shaded twice by the shade plugin.
The correct fix or workaround has not been identified, but the warnings are harmless.

The install step will have installed into your local repository these artificts:

* `Astronomy_-4.0-SNAPSHOT.jar`
* `ij-4.0-SNAPSHOT.jar`
* `bislider-1.4.1.jar`
* `flanagan-1.0.20121106.jar`
* `GacsConnection-2.0.jar`

Setting `classpathScope` to `test` is a workaround for the fact that the `exec-maven-plugin`
does not provide jars with scope `provided`.
An ImageJ plugin has much the same relationship to ImageJ as a Java enterprise application has
to an enterprise application server. Since we aren't
deploying into a running copy of ImageJ, ImageJ has to be provided.

Project Structure
-----------------

The AstroImageJ project is a Maven aggregator project first and foremost containing two Maven modules:

* `plugin`
* [`submodules/imagej-ImageJA`](https://github.com/observatree/imagej-ImageJA)

From the perspective of git, the plugin directory is part of the repo whereas the submodules/imagej-ImageJA directory
is a separate repo incorporated using the usual git submodule machinery. (It is important to keep track of
the distinction between git's notion of submodules and Maven's notion of modules.)

In addition the aggregator project includes three more Maven modules (each in a git submodule) in order to build the
bislider, flanagan, and GacsConnection artifacts (none of which are in Maven Central):

* [`submodules/fredericdevernier-bislider`](https://github.com/observatree/fredericdevernier-bislider)
* [`submodules/michaeltflanagan-flanagan`](https://github.com/observatree/michaeltflanagan-flanagan)
* [`submodules/esdc-tapclient`](https://github.com/observatree/esdc-tapclient)

A final git submodule contains the beginnings of a system test plan and resources for executing it:

* [`submodules/aij-testing`](https://github.com/observatree/aij-testing)

While we are on the subject of testing, it should be noted that there are no unit tests in any of the codebases,
and this represents a large amount of technical debt.

Repository History
------------------

The present project structure resulted from an examination of
[ImageJforAstroImageJ](http://github.com/karenacollins/ImageJforAstroImageJ).
That project diverged from its upstream source in 2013 (ImageJ v1.47i), and 
in any case was not an actual fork of the upstream repository, and therefore
it could not automatically receive updates.

Since the ImageJforAstroImageJ codebase had not received any updates from NIH since 1.47i
(at which time ImageJ was running on Java 6), and meanwhile NIH's ImageJA project had gone
through dozens of releases, by far the greater share of the changes to the diverged codebases were
easiest to include by taking a current fork of NIH's repo:

* [`ImageJA`](https://github.com/imagej/ImageJA)

The master branch at tag 1.47i was used to create a branch named ai8. Karen Collins' changes to this 
code were then manually applied to this branch. The next step was to merge all of the
changes to ImageJA in master from v1.47i to v.1.52i into the ai8 branch. There were over 80,000 lines of diffs,
most of which were merged automatically. About 30 merge conflicts in 13 files were resolved by inspection.

The changes in imagej-ImageJA needed for AstroImageJ are very specific to the AstroImageJ plugins and therefore very
unlikely to ever be pushed upstream. Therefore in ImageJA we will continue to do our work on the ai8 branch.
The master branch is reserved for accepting upstream changes from NIH, which will
periodically be merged into the ai8 branch.

## Packaging

After any run-time issues have been dealt with, the final step will be to deal with platform-specific (Linux, macOS, and Windows) packaging.

See [AppPackaging.md](./AppPackaging.md).
