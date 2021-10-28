package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.execution.configurations.RunConfigurationOptions

class DotNetWatchRunConfigurationOptions : RunConfigurationOptions() {

    private var projectNameOption = string("").provideDelegate(this, "projectName")

    var projectName: String?
        get() = projectNameOption.getValue(this)
        set(value) = projectNameOption.setValue(this, value)
}