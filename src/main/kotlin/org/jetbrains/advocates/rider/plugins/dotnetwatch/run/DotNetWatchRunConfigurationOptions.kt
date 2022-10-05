@file:Suppress("UnstableApiUsage")

package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.project.Project
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.jetbrains.rider.projectView.workspace.getProjectModelEntities
import com.jetbrains.rider.projectView.workspace.isUnloadedProject
import java.nio.file.Path

class DotNetWatchRunConfigurationOptions : RunConfigurationOptions() {

    private var projectFilePathOption = string("").provideDelegate(this, "projectFilePath")
    private var trackProjectExePathOption = property(true).provideDelegate(this, "trackProjectExePath")
    private var trackProjectWorkingDirectoryOption = property(true).provideDelegate(this, "trackProjectWorkingDirectory")
    private var projectTfmOption = string("").provideDelegate(this, "projectTfm")
    private var exePathOption = string("").provideDelegate(this, "exePath")
    private var programParametersOption = string("").provideDelegate(this, "programParameters")
    private var workingDirectoryOption = string("").provideDelegate(this, "workingDirectory")
    private var envsOption = map<String, String>().provideDelegate(this, "envs")
    private var isPassParentEnvsOption = property(true).provideDelegate(this, "isPassParentEnvs")
    private var useExternalConsoleOption = property(false).provideDelegate(this, "useExternalConsole")
    private var verbosityOption = string(DotNetWatchVerbosity.NORMAL.name).provideDelegate(this, "verbosity")
    private var isSuppressHotReloadOption = property(false).provideDelegate(this, "isSuppressHotReload")
    private var isRestartOnRudeEditEditorOption = property(false).provideDelegate(this, "isRestartOnRudeEditEditor")
    private var isUsePollingFileWatcherOption = property(false).provideDelegate(this, "isUsePollingFileWatcher")
    private var isSuppressBrowserLaunchOption = property(false).provideDelegate(this, "isSuppressBrowserLaunch")
    private var isSuppressBrowserRefreshOption = property(false).provideDelegate(this, "isSuppressBrowserRefresh")

    var projectFilePath: String
        get() = projectFilePathOption.getValue(this) ?: ""
        set(value) = projectFilePathOption.setValue(this, value)

    var trackProjectExePath: Boolean
        get() = trackProjectExePathOption.getValue(this)
        set(value) = trackProjectExePathOption.setValue(this, value)

    var trackProjectWorkingDirectory: Boolean
        get() = trackProjectWorkingDirectoryOption.getValue(this)
        set(value) = trackProjectWorkingDirectoryOption.setValue(this, value)

    var projectTfm: String
        get() = projectTfmOption.getValue(this) ?: ""
        set(value) = projectTfmOption.setValue(this, value)

    var exePath: String
        get() = exePathOption.getValue(this) ?: ""
        set(value) = exePathOption.setValue(this, value)

    var programParameters: String
        get() = programParametersOption.getValue(this) ?: ""
        set(value) = programParametersOption.setValue(this, value)

    var workingDirectory: String
        get() = workingDirectoryOption.getValue(this) ?: ""
        set(value) = workingDirectoryOption.setValue(this, value)

    var envs: Map<String, String>
        get() = envsOption.getValue(this)
        set(value) = envsOption.setValue(this, value.toMutableMap())

    var isPassParentEnvs: Boolean
        get() = isPassParentEnvsOption.getValue(this)
        set(value) = isPassParentEnvsOption.setValue(this, value)

    var useExternalConsole: Boolean
        get() = useExternalConsoleOption.getValue(this)
        set(value) = useExternalConsoleOption.setValue(this, value)

    var verbosity: DotNetWatchVerbosity
        get() = DotNetWatchVerbosity.valueOf(verbosityOption.getValue(this) ?: DotNetWatchVerbosity.NORMAL.name)
        set(value) = verbosityOption.setValue(this, value.name)

    var isSuppressHotReload: Boolean
        get() = isSuppressHotReloadOption.getValue(this)
        set(value) = isSuppressHotReloadOption.setValue(this, value)

    var isRestartOnRudeEditEditor: Boolean
        get() = isRestartOnRudeEditEditorOption.getValue(this)
        set(value) = isRestartOnRudeEditEditorOption.setValue(this, value)

    var isUsePollingFileWatcher: Boolean
        get() = isUsePollingFileWatcherOption.getValue(this)
        set(value) = isUsePollingFileWatcherOption.setValue(this, value)

    var isSuppressBrowserLaunch: Boolean
        get() = isSuppressBrowserLaunchOption.getValue(this)
        set(value) = isSuppressBrowserLaunchOption.setValue(this, value)

    var isSuppressBrowserRefresh: Boolean
        get() = isSuppressBrowserRefreshOption.getValue(this)
        set(value) = isSuppressBrowserRefreshOption.setValue(this, value)

    fun isUnloadedProject(project: Project) = WorkspaceModel.getInstance(project)
        .getProjectModelEntities(Path.of(projectFilePath), project)
        .any { it.isUnloadedProject() }
}