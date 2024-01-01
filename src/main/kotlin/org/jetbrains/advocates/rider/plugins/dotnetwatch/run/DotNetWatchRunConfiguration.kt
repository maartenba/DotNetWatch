package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import com.jetbrains.rider.debugger.showElevationDialogIfNeeded
import com.jetbrains.rider.projectView.solutionDirectory
import com.jetbrains.rider.run.*
import com.jetbrains.rider.run.configurations.BuildAwareRunConfiguration
import com.jetbrains.rider.run.configurations.project.DotNetProjectConfigurationType
import com.jetbrains.rider.run.configurations.runnableProjectsModelIfAvailable
import com.jetbrains.rider.runtime.RiderDotNetActiveRuntimeHost
import com.jetbrains.rider.util.idea.getService
import java.io.File

class DotNetWatchRunConfiguration(project: Project, factory: ConfigurationFactory, name: String)
    : RunConfigurationBase<DotNetWatchRunConfigurationOptions>(project, factory, name), BuildAwareRunConfiguration, DumbAware {

    private val riderDotNetActiveRuntimeHost = project.getService<RiderDotNetActiveRuntimeHost>()

    fun watchOptions() = options

    // For filtering list of projects - show only console/web/wcf/dotnetcore, not launchsettings etc.
    private val type = DotNetProjectConfigurationType()

    override fun getOptions(): DotNetWatchRunConfigurationOptions =
        super.getOptions() as DotNetWatchRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = DotNetWatchRunConfigurationEditor(project)

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(executionEnvironment) {

            override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
                try {
                    val dotNetExePath = riderDotNetActiveRuntimeHost.dotNetCoreRuntime.value
                        ?: throw ExecutionException("Could not determine active .NET runtime.")

                    // Create command line
                    val commandLine = createEmptyConsoleCommandLine(options.useExternalConsole)
                    commandLine.exePath = dotNetExePath.cliExePath
                    commandLine.addParameters(
                        listOf(
                            "watch",
                            "run",
                            "--project", options.projectFilePath
                        )
                    )

                    // Determine target framework version parameter
                    project.runnableProjectsModelIfAvailable?.projects?.valueOrNull?.let { runnableProjects ->
                        val runnableProject = runnableProjects.singleOrNull {
                            it.projectFilePath == options.projectFilePath && type.isApplicable(it.kind)
                        } ?: return@let

                        // Use configured/available TFM
                        if (runnableProject.projectOutputs.size > 1 && runnableProject.projectOutputs.any { it.tfm?.presentableName == options.projectTfm }) {
                            commandLine.addParameters("--framework", options.projectTfm)
                        }
                    }

                    options.verbosity.argumentValue?.let { commandLine.addParameter(it) }

                    if (options.isSuppressHotReload) {
                        commandLine.addParameter("--no-hot-reload")
                    }

                    if (options.watchParameters.isNotEmpty()) {
                        commandLine.parametersList.addAll(
                            ParametersListUtil.parse(options.watchParameters))
                    }

                    if (options.programParameters.isNotEmpty()) {
                        commandLine.addParameter("--")
                        commandLine.parametersList.addAll(
                            ParametersListUtil.parse(options.programParameters))
                    }

                    val workingDirectory = File(options.workingDirectory)
                    commandLine.workDirectory = if (workingDirectory.exists()) workingDirectory else project.solutionDirectory
                    commandLine.withParentEnvironmentType(if (options.isPassParentEnvs) GeneralCommandLine.ParentEnvironmentType.CONSOLE else GeneralCommandLine.ParentEnvironmentType.NONE)
                    commandLine.withEnvironment(options.envs)

                    if (options.isRestartOnRudeEditEditor) {
                        commandLine.withEnvironment("DOTNET_WATCH_RESTART_ON_RUDE_EDIT", "true")
                    }
                    if (options.isUsePollingFileWatcher) {
                        commandLine.withEnvironment("DOTNET_USE_POLLING_FILE_WATCHER", "true")
                    }
                    if (options.isSuppressBrowserLaunch) {
                        commandLine.withEnvironment("DOTNET_WATCH_SUPPRESS_LAUNCH_BROWSER", "true")
                    }
                    if (options.isSuppressBrowserRefresh) {
                        commandLine.withEnvironment("DOTNET_WATCH_SUPPRESS_BROWSER_REFRESH", "true")
                    }

                    // Start process
                    val processHandler = if (options.useExternalConsole)
                        ExternalConsoleMediator.createProcessHandler(commandLine)
                    else
                        TerminalProcessHandler(project, commandLine)

                    ProcessTerminatedListener.attach(processHandler)

                    // Create console
                    val console = createConsole(
                        consoleKind = if (options.useExternalConsole) ConsoleKind.ExternalConsole else ConsoleKind.Normal,
                        processHandler = processHandler,
                        project = project
                    )

                    return DefaultExecutionResult(console, processHandler, *AnAction.EMPTY_ARRAY)
                } catch (t: Throwable) {
                    showElevationDialogIfNeeded(t, environment.project)
                    throw ExecutionException(t)
                }
            }

            override fun startProcess(): ProcessHandler {
                @Suppress("DialogTitleCapitalization")
                throw ExecutionException("startProcess() should not be called.")
            }
        }
    }

    override fun mustBeStoppedToRunBuild(): Boolean = false
}