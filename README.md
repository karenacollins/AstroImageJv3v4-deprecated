# AIJ - Port of AstroImageJ to Java 8

The quick start below assumes you have a command-line development environment set up, including Git, Java 8 and Maven.

If you don't have that, try these directions: [DevelopmentEnvironment.md](./docs/DevelopmentEnvironment.md).

## Quick Start

Five codebases, consisting of ImageJ and AstroImageJ and three dependency libraries,
are cloned simultaneously with the following command:

```
$ git clone --recursive git@github.com:observatree/AstroImageJ.git
```

To build the project:

```
$ cd AstroImageJ
$ mvn install
```

Although it is probably preferable to launch the project
from an IDE (see [Development Environment](./docs/DevelopmentEnvironment.md)),
it can be launched from the command line by doing:

```
$ cd plugin
$ mvn exec:java -Dexec.mainClass="ij.ImageJ" -Dexec.classpathScope=test
```

Additional information about the Quick Start and the project structure
is in [Project Structure](./docs/ProjectStructure.md).
