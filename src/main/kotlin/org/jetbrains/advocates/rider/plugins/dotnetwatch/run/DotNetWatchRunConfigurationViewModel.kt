package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import com.jetbrains.rd.ide.model.EnvironmentVariable
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.reactive.adviseOnce
import com.jetbrains.rider.model.*
import com.jetbrains.rider.run.configurations.controls.*
import com.jetbrains.rider.run.configurations.dotNetExe.DotNetExeConfigurationViewModel
import java.io.File

class DotNetWatchRunConfigurationViewModel(
    private val lifetime: Lifetime,
    private val project: Project,
    private val runnableProjectsModel: RunnableProjectsModel,
    val projectSelector: ProjectSelector,
    val tfmSelector: StringSelector,
    programParametersEditor: ProgramParametersEditor,
    environmentVariablesEditor: EnvironmentVariablesEditor,
    useExternalConsoleEditor: FlagEditor,
    dotnetWatchSeparator: ViewSeparator,
    val verbosityEditor: EnumSelector<DotNetWatchVerbosity>,
    val isSuppressHotReloadEditor: FlagEditor,
) : DotNetExeConfigurationViewModel(
    lifetime = lifetime,
    project = project,
    exePathSelector = PathSelector("", null, lifetime),
    programParametersEditor = programParametersEditor,
    workingDirectorySelector = PathSelector("", null, lifetime),
    environmentVariablesEditor = environmentVariablesEditor,
    runtimeSelector = RuntimeSelector(""),
    runtimeArgumentsEditor = ProgramParametersEditor("", lifetime),
    trackExePathInWorkingDirectoryIfItPossible = false,
    useExternalConsoleEditor = useExternalConsoleEditor) {

    private var isLoaded = false

    override val controls: List<ControlBase> = listOf(
        projectSelector,
        tfmSelector,
        programParametersEditor,
        environmentVariablesEditor,
        useExternalConsoleEditor,
        dotnetWatchSeparator,
        verbosityEditor,
        isSuppressHotReloadEditor
    )

    init {
        projectSelector.bindTo(
            runnableProjectsModel = runnableProjectsModel,
            lifetime = lifetime,
            projectFilter = { true },
            onLoad = ::enable,
            onSelect = ::handleProjectSelection
        )

        tfmSelector.string.advise(lifetime) { handleChangeTfmSelection() }
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
                exePathSelector.path.set(projectOutput.exePath)
                workingDirectorySelector.path.set(projectOutput.workingDirectory)
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

    fun reset(projectFilePath: String,
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

        verbosityEditor.rawValue.set(verbosity)
        isSuppressHotReloadEditor.isSelected.set(isSuppressHotReload)

        runnableProjectsModel.projects.adviseOnce(lifetime) { projectList ->

            if (projectFilePath.isEmpty() || projectList.none { it.projectFilePath == projectFilePath }) {
                // Case when project is not selected - generate a fake entry
                if (projectFilePath.isEmpty() || !isUnloadedProject) {
                    projectList.firstOrNull()?.let { project ->
                        projectSelector.project.set(project)
                        isLoaded = true
                        handleProjectSelection(project)
                    }
                } else {
                    val fakeProjectName = File(projectFilePath).name
                    val fakeProject = RunnableProject(
                        fakeProjectName, fakeProjectName, projectFilePath, RunnableProjectKind.Unloaded,
                        listOf(
                            ProjectOutput(
                                RdTargetFrameworkId("", projectTfm, false, false), exePath,
                                ParametersListUtil.parse(programParameters), workingDirectory, "", null
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
                projectList.singleOrNull { it.projectFilePath == projectFilePath  }?.let { runnableProject ->
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