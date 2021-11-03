package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.jetbrains.rd.platform.util.getComponent
import com.jetbrains.rider.projectView.solutionDirectory
import com.jetbrains.rider.runtime.RiderDotNetActiveRuntimeHost

class DotNetWatchRunConfiguration(project: Project, factory: ConfigurationFactory, name: String)
    : RunConfigurationBase<DotNetWatchRunConfigurationOptions>(project, factory, name) {

    private val riderDotNetActiveRuntimeHost = project.getComponent<RiderDotNetActiveRuntimeHost>()

    fun watchOptions() = options

    override fun getOptions(): DotNetWatchRunConfigurationOptions =
        super.getOptions() as DotNetWatchRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = DotNetWatchRunConfigurationEditor(project)

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(executionEnvironment) {
            override fun startProcess(): ProcessHandler {
                val commandLine = riderDotNetActiveRuntimeHost.dotNetCoreRuntime.value?.createCommandLine(
                    listOf(
                        "watch",
                        if (options.isVerbose) "--verbose" else "",
                        "run",
                        "--project", options.projectFilePath,
                        "--framework", options.projectTfm,
                        "--", options.programParameters,
                    )
                ) ?: throw ExecutionException("Could not determine active .NET runtime.")

                commandLine.workDirectory = project.solutionDirectory
                commandLine.withParentEnvironmentType(if (options.isPassParentEnvs) GeneralCommandLine.ParentEnvironmentType.CONSOLE else GeneralCommandLine.ParentEnvironmentType.NONE)
                commandLine.withEnvironment(options.envs)

                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }
}