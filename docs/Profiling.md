Profiling
=========

An "asynchronous profiler" is an experimental (meaning hidden!)
feature of IntelliJ 2018.3. Its existence is worth documenting
because it is easy to write Java code that is too slow when
processing images, and a profiler is the fastest way to discover
opportunities for optimization.

Call stacks in an asynchronous profiler or an ordinary profiler are
presented two ways: from any method that is in the sampled methods you
can ask "what called this?" or you can ask, "what does this call?"
Both answers are given in terms of weighted lists of methods (the
weight is the number of times the corresponding backtrace was
sampled). Both are handy and are offered by IntelliJ's 
asynchronous profiler.

Asynchronous profiling in Java perhaps owes a lot to asynchronous
profiling in Objective-C which Bertrand Serlet pioneered on
early versions of macOS. It was originally a separate app called
Sampler, and eventually become part of Xcode Instruments.

Among the things that Bertrand's profiler could do is monitor object
allocation. Excessive heap allocation can make apps sluggish and won't
show up as CPU utilization, because it is easy to overwhelm the
processor's cache memory, and the result is that the processor is just
waiting on the main memory - or worse yet, the disk. A way to mimic
monitoring of heap allocation is to profile calls to suspect memory
allocation methods.

Not only is asynchronous profiling more systematically accurate than
ordinary profiling (albeit with lower precision because it deliberaly
introduces an element of randomness into the sampling), unlike
ordinary profiling it requires no recompilation! Instead it uses
exactly the same "watch another process and selectively
stop and interrogate its threads" capability that a debugger requires.

Here is the article announcing the IntelliJ asynchronous profiler:

https://blog.jetbrains.com/idea/2018/09/intellij-idea-2018-3-eap-git-submodules-jvm-profiler-macos-and-linux-and-more/

Asynchronous profiling in IntelliJ is *not* available on the Windows
build. It is only available on the macOS and Linux builds, and then
only if experimental features are turned on.

Return to [top-level README](../README.md).
