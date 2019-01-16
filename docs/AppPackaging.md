Application Packaging
=====================

Packaging AstroImageJ
---------------------

As distributed for macOS by NIH, the top-level ImageJ directory contains:

```
jre/
luts/
macros/
plugins/
ImageJ.app/
Installation Notes.html
```

The most important item in the app-wrapper is Java/ij.jar. _Examples_ of files inside this jar are:

```
IJ_Props.txt
macros/TimeStamp.ijm
about.jpg
```

and


```
MacAdapter.class
ij/CommandListener.class
ij/gui/ColorChooser.class
```

Thanks to the extensive system of Maven defaults, the packaging of ij.jar occurs without any additional pom.xml configuration.

All resources in src/main/resources including the examples in the first group end up in target/classes as a result of:

```
jar lifecycle
process-resources phase
resources:resources goal
```

All class files compiled from src/main/java including the examples in the second group end up in target/classes as a result of:

```
jar lifecycle
compile phase
compiler:compile goal
```

Packaging the AstroImageJ Plugins
---------------------------------

As distributed by Karen Collins (see her [distribution directory](http://www.astro.louisville.edu/software/astroimagej/updates) for releases back to 2013-01-24), Astronomy_.jar contains resources, _examples_ of which are:

```
astroj/help/multiplot_help.htm
astroj/images/align.png
```

and class files, _examples_ of which are:

```
Align_Image.class
astroj/AnnotateRoi.class
```

Using standard Maven defaults, the above will be included in Astronomy._jar without any additional pom.xml configuration.

In addition, Astronomy_.jar contained the sources and class files for the following five third-party dependencies:

```
Jama-1.0.3.jar
flanagan-1.0.2021106.jar
bislider-1.4.1.jar
```

and

```
json-simple-1.1.1.jar
BrowserLauncher2-1.3.jar
```

The former group of three we must do Maven builds for locally because they are not present in Maven Central. The latter group of two is present in Maven Central. All five jars for third-party dependencies must be bundled into Astronomy_.jar using the Maven Shade plug-in. This is because the plugin class loader is not prepared to search for 
any jars other than the the plug-in jars themselves. Those are distinguished by having an underscore in their names.

Return to [top-level README](../README.md).
