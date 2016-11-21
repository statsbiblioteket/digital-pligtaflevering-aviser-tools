The bin/* scripts are launchable from any directory.

Configuration files in conf/ are present on the classpath.

It is strongly suggested that relative file paths are
used for files to be written, and that the current
working directory is set to be the location where the
files are to be written before invoking a launcher script.

Examples could be log files, crash dump files, JVM diagnostic files etc.

So something like:

    cd ..../logs-for-foo
    ..../bin/foo ....


/del_2016-08-26



