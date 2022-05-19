package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import com.jetbrains.rd.ide.model.EnvironmentVariable
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.reactive.adviseOnce
import com.jetbrains.rider.model.*
import com.jetbrains.rider.run.configurations.RunnableProjectKinds
import com.jetbrains.rider.run.configurations.controls.*
import com.jetbrains.rider.run.configurations.dotNetExe.DotNetExeConfigurationViewModel
import com.jetbrains.rider.run.configurations.project.DotNetProjectConfigurationType
import java.io.File

class DotNetWatchRunConfigurationViewModel(
    private val lifetime: Lifetime,
    private val project: Project,
    private val runnableProjectsModel: RunnableProjectsModel,
    val projectSelector: ProjectSelector,
    val tfmSelector: StringSelector,
    programParametersEditor: ProgramParametersEditor,
    workingDirectorySelector: PathSelector,
    environmentVariablesEditor: EnvironmentVariablesEditor,
    useExternalConsoleEditor: FlagEditor,
    dotnetWatchSeparator: ViewSeparator,
    val verbosityEditor: EnumSelector<DotNetWatchVerbosity>,
    val isSuppressHotReloadEditor: FlagEditor,
    val isRestartOnRudeEditEditor: FlagEditor
) : DotNetExeConfigurationViewModel(
    lifetime = lifetime,
    project = project,
    exePathSelector = PathSelector("", "Exe_path", null, lifetime),
    programParametersEditor = programParametersEditor,
    workingDirectorySelector = workingDirectorySelector,
    environmentVariablesEditor = environmentVariablesEditor,
    runtimeSelector = RuntimeSelector("", "Runtime"),
    runtimeArgumentsEditor = ProgramParametersEditor("", "Runtime_arguments", lifetime),
    trackExePathInWorkingDirectoryIfItPossible = false,
    useExternalConsoleEditor = useExternalConsoleEditor) {

    private var isLoaded = false

    // For filtering list of projects - show only console/web/wcf/dotnetcore, not launchsettings etc.
    private val type = DotNetProjectConfigurationType()

    var trackProjectExePath: Boolean = true
    var trackProjectWorkingDirectory: Boolean = true

    override val controls: List<ControlBase> = listOf(
        projectSelector,
        tfmSelector,
        programParametersEditor,
        workingDirectorySelector,
        environmentVariablesEditor,
        useExternalConsoleEditor,
        dotnetWatchSeparator,
        verbosityEditor,
        isSuppressHotReloadEditor,
        isRestartOnRudeEditEditor
    )

    init {
        disable()

        projectSelector.bindTo(
            runnableProjectsModel = runnableProjectsModel,
            lifetime = lifetime,
            projectFilter = { p -> type.isApplicable(p.kind) },
            onLoad = ::enable,
            onSelect = ::handleProjectSelection
        )

        tfmSelector.string.advise(lifetime) { handleChangeTfmSelection() }
        exePathSelector.path.advise(lifetime) { recalculateTrackProjectOutput() }
        workingDirectorySelector.path.advise(lifetime) { recalculateTrackProjectOutput() }
    }

    private fun handleProjectSelection(runnableProject: RunnableProject) {
        if (!isLoaded) return
        reloadTfmSelector(runnableProject)

        environmentVariablesEditor.envs.set(runnableProject.environmentVariables.associate { it.key to it.value })
    }

    private fun handleChangeTfmSelection() {
        projectSelector.project.valueOrNull?.projectOutputs
            ?.singleOrNull { it.tfm?.presentableName == tfmSelector.string.valueOrNull }
            ?.let { projectOutput ->
                // Tracked values
                if (trackProjectExePath)
                    exePathSelector.path.set(projectOutput.exePath)

                if (trackProjectWorkingDirectory)
                    workingDirectorySelector.path.set(projectOutput.workingDirectory)

                // Update default values (e.g. when folks reset the model)
                exePathSelector.defaultValue.set(projectOutput.exePath)
                workingDirectorySelector.defaultValue.set(projectOutput.workingDirectory)
            }
    }

    private fun reloadTfmSelector(runnableProject: RunnableProject) {
        tfmSelector.stringList.clear()
        runnableProject.projectOutputs.map { it.tfm?.presentableName ?: "" }.sorted().forEach {
            tfmSelector.stringList.add(it)
        }
        if (tfmSelector.stringList.isNotEmpty()) {
            tfmSelector.string.set(tfmSelector.stringList.first())
        }
        handleChangeTfmSelection()
    }

    private fun recalculateTrackProjectOutput() {
        val selectedProject = projectSelector.project.valueOrNull ?: return
        val selectedTfm = tfmSelector.string.valueOrNull ?: return

        selectedProject.projectOutputs.singleOrNull { it.tfm?.presentableName == selectedTfm }?.let { projectOutput ->
            trackProjectExePath = exePathSelector.path.value.isEmpty() || exePathSelector.path.value == projectOutput.exePath
            trackProjectWorkingDirectory = workingDirectorySelector.path.value.isEmpty() || workingDirectorySelector.path.value == projectOutput.workingDirectory
        }
    }

    fun reset(projectFilePath: String,
              trackProjectExePath: Boolean,
              trackProjectWorkingDirectory: Boolean,
              projectTfm: String,
              exePath: String,
              programParameters: String,
              workingDirectory: String,
              envs: Map<String, String>,
              passParentEnvs: Boolean,
              useExternalConsole: Boolean,
              isUnloadedProject: Boolean,
              verbosity: DotNetWatchVerbosity,
              isSuppressHotReload: Boolean,
              isRestartOnRudeEdit: Boolean
    ) {
        fun resetProperties(exePath: String, programParameters: String, workingDirectory: String) {
            super.reset(
                exePath = exePath,
                programParameters = programParameters,
                workingDirectory = workingDirectory,
                envs = envs,
                isPassParentEnvs = passParentEnvs,
                runtime = null,
                runtimeOptions = "",
                useExternalConsole = useExternalConsole
            )
        }

        isLoaded = false

        this.trackProjectExePath = trackProjectExePath
        this.trackProjectWorkingDirectory = trackProjectWorkingDirectory

        verbosityEditor.rawValue.set(verbosity)
        isSuppressHotReloadEditor.isSelected.set(isSuppressHotReload)
        isRestartOnRudeEditEditor.isSelected.set(isRestartOnRudeEdit)

        runnableProjectsModel.projects.adviseOnce(lifetime) { projectList ->

            if (projectFilePath.isEmpty() || projectList.none {
                    it.projectFilePath == projectFilePath && type.isApplicable(it.kind) }) {

                // Case when project is not selected - generate a fake entry
                if (projectFilePath.isEmpty() || !isUnloadedProject) {
                    projectList.firstOrNull { type.isApplicable(it.kind) }?.let { project ->
                        projectSelector.project.set(project)
                        isLoaded = true
                        handleProjectSelection(project)
                    }
                } else {
                    val fakeProjectName = File(projectFilePath).name
                    val fakeProject = RunnableProject(
                        fakeProjectName, fakeProjectName, projectFilePath, RunnableProjectKinds.Unloaded,
                        listOf(
                            ProjectOutput(
                                RdTargetFrameworkId("", projectTfm, false, false), exePath,
                                ParametersListUtil.parse(programParameters), workingDirectory, "", null, emptyList()
                            )
                        ),
                        envs.map { EnvironmentVariable(it.key, it.value) }.toList(), null, listOf()
                    )
                    projectSelector.projectList.apply {
                        clear()
                        addAll(projectList + fakeProject)
                    }
                    projectSelector.project.set(fakeProject)
                    reloadTfmSelector(fakeProject)
                    resetProperties(
                        exePath = exePath,
                        programParameters = programParameters,
                        workingDirectory = workingDirectory)
                }
            } else {
                projectList.singleOrNull {
                    it.projectFilePath == projectFilePath && type.isApplicable(it.kind)
                }?.let { runnableProject ->

                    projectSelector.project.set(runnableProject)

                    // Set TFM
                    reloadTfmSelector(runnableProject)
                    val projectTfmExists = runnableProject.projectOutputs.any { it.tfm?.presentableName == projectTfm }
                    val selectedTfm = if (projectTfmExists) projectTfm else runnableProject.projectOutputs.firstOrNull()?.tfm?.presentableName ?: ""
                    tfmSelector.string.set(selectedTfm)

                    // Set Project Output
                    val projectOutput = runnableProject.projectOutputs.singleOrNull { it.tfm?.presentableName == selectedTfm }
                    val effectiveExePath = projectOutput?.exePath ?: exePath
                    val effectiveProgramParameters =
                        if (projectOutput != null && projectOutput.defaultArguments.isNotEmpty())
                            ParametersListUtil.join(projectOutput.defaultArguments).replace("\\\"", "\"")
                        else if (programParameters.isNotEmpty())
                            programParameters
                        else
                            // Handle the case when program parameters were set by changing TFM above and make sure it is not reset to empty.
                            programParametersEditor.defaultValue.value

                    programParametersEditor.defaultValue.set(effectiveProgramParameters)

                    val effectiveWorkingDirectory = projectOutput?.workingDirectory ?: workingDirectory

                    // Reset properties
                    resetProperties(
                        exePath = effectiveExePath,
                        programParameters = effectiveProgramParameters,
                        workingDirectory = effectiveWorkingDirectory)
                }
            }
            isLoaded = true
        }
    }
}