# AIJ - Port of AstroImageJ to Java 8

The directions below assume you have a command-line development environment set up.

If you don't have that, try these directions: [DevelopmentEnvironment.md](./docs/DevelopmentEnvironment.md).

Getting the Sources
-------------------

Six codebases, consisting of ImageJA and AstroImageJ and four dependency libraries,
are checked out simultaneously with the following command:

```
$ git clone --recursive git@github.com:observatree/karenacollins-AstroImageJ.git
```

Additionally, the preceding command will check out a system test or "smoke test" plan.

This project structure is discussed under [Project Structure and History](#project-structure-and-history) below.

Building the Submodules and AstroImageJ
---------------------------------------

Change directories each of the following five submodules:

* `submodules/imagej-ImageJA`
* `submodules/frederic-devernier-bislider`
* `submodules/usnistgov-jama`
* `submodules/michaeltflanagan-flanagan`
* `submodules/esdc-tapclient`

and in each of them do:

```
$ mvn install
$ mvn source:jar install
```

Your local Maven repository will then contain:

* ```ij-4.0-SNAPSHOT.jar```
* ```bislider-1.4.1.jar```
* ```jama-1.0.3.jar```
* ```flanagan-1.0.20121106.jar```
* ```GacsConnection-2.0.jar```

and the corresponding source jars.

Finally, in the top-level directory, do:

```
$ mvn install
$ mvn source:jar install
```

## Project Structure and History

The AIJ project is a wrapper project containing two codebases in submodules: ImageJA and AstroImageJ.
The project began with an examination of [ImageJforAstroImageJ](http://github.com/karenacollins/ImageJforAstroImageJ).
That project diverged from its upstream source in 2013 (ImageJ v1.47i).

Since the ImageJforAstroImageJ codebase had not received any updates from NIH since 1.47i
(at which time ImageJ was running on Java 6), and meanwhile NIH's ImageJA project had gone
through dozens of releases, by far the greater share of the changes to the diverged codebases were
easiest to include by taking a new and current fork of NIH's repo:

* [imagej-ImageJA](https://github.com/observatree/imagej-ImageJA) (forked from https://github.com/imagej/ImageJA)

Karen Collins' changes to v1.47i were placed into a branch named ai8. The next step was to merge all of the
changes to ImageJA in master from v1.47i to v.1.52i into the ai8 branch. There were over 80,000 lines of diffs,
most of which were merged automatically. About 30 merge conflicts in 13 files were resolved by inspection.

The changes in ImageJforAstroImageJ are very specific to the AstroImageJ plugins and therefore very
unlikely to ever be pushed upstream. Therefore in ImageJA we will continue to do our work on the ai8 branch.
The master branch is reserved for accepting upstream changes from NIH, which will
periodically be merged into the ai8 branch.

The source for the jars, `bislider`, `jama`, `flanagan`, and `GacsConnection` is included in submodules because these
jars are not in Maven central, and this is the easiest and most automated way to get them built locally.

A final submodule contains a rudimentary system test plan and resources for executing it:

* [aij-testing](https://github.com/observatree/aij-testing)

While we are on the subject of testing, it should be noted that there are no unit tests in most of the codebases, and this
represents a large amount of technical debt.

## Packaging

After any run-time issues have been dealt with, the final step will be to deal with platform-specific (Linux, macOS, and Windows) packaging.

See [AppPackaging.md](./docs/AppPackaging.md).
