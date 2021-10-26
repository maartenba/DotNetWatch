package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.execution.configurations.RunConfigurationOptions

class DotNetWatchRunConfigurationOptions : RunConfigurationOptions() {

    private var myScriptName = string("").provideDelegate(this, "scriptName")

    var scriptName: String?
        get() = myScriptName.getValue(this)
        set(value) = myScriptName.setValue(this, value)
}