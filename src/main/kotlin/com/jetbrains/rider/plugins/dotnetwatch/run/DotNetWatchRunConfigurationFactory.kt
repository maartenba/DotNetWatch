package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class DotNetWatchRunConfigurationFactory(configurationType: DotNetWatchRunConfigurationType)
    : ConfigurationFactory(configurationType) {

    override fun getId() = DotNetWatchRunConfigurationType.ID

    override fun createTemplateConfiguration(project: Project) =
        DotNetWatchRunConfiguration(project, this, "Run dotnet-watch")

    override fun getOptionsClass(): Class<out BaseState> = DotNetWatchRunConfigurationOptions::class.java
}