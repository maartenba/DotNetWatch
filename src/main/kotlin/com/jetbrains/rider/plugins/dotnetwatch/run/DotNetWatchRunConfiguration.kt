package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.solutionDirectory

class DotNetWatchRunConfiguration(project: Project, factory: ConfigurationFactory, name: String)
    : RunConfigurationBase<DotNetWatchRunConfigurationOptions>(project, factory, name) {

    fun getDotNetWatchOptions() = options

    override fun getOptions(): DotNetWatchRunConfigurationOptions =
        super.getOptions() as DotNetWatchRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = DotNetWatchRunConfigurationEditor()

    override fun checkConfiguration() {

        //if (options.scriptName.isNullOrEmpty()) throw ConfigurationException("Script name should not be empty.")

        super.checkConfiguration()
    }

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(executionEnvironment) {
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine("dotnet", "watch", "--project", options.scriptName)
                commandLine.workDirectory = project.solutionDirectory

                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }
}