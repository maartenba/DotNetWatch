package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

enum class DotNetWatchVerbosity(val argumentValue: String?) {
    NORMAL(null),
    QUIET("--quiet"),
    VERBOSE("--verbose")
}