package com.matteoveroni.bun_gradle_plugin.gradle.plugins

import com.matteoveroni.bun_gradle_plugin.gradle.extensions.BunExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec

class BunFrontendPlugin implements Plugin<Project> {

    public static final String TASK_NAME_INSTALL_BUN_IF_NEEDED = "installBunIfNeeded"

    @Override
    void apply(Project project) {
        project.extensions.create("bun", BunExtension)

        final def bunInstallationMetadataFile = project.layout.buildDirectory.file("bun/bun_installed_on_system.txt")

        def taskClearBunMetadataFile = project.tasks.register("clearBunMetadataFile", Delete) {
            group = "bun"
            delete bunInstallationMetadataFile
        }

        def taskCheckIfBunInstalled = project.tasks.register("checkIfBunInstalled", Exec) {
            group = "bun"
            dependsOn(taskClearBunMetadataFile)
            setIgnoreExitValue(true)
            commandLine 'bash', '-ci', 'bun -v'
            standardOutput = new ByteArrayOutputStream()
            errorOutput = new ByteArrayOutputStream()
            outputs.file(bunInstallationMetadataFile)
            doLast {
                if (executionResult.get().exitValue == 0) {
                    logger.info("Bun is installed on the system. Bun version: ${standardOutput.toString()}")
                    outputs.files.singleFile.write("YES")
                } else {
                    logger.info("Bun is not installed on the system.")
                    outputs.files.singleFile.write("NO")
                }
            }
        }

        project.tasks.register(TASK_NAME_INSTALL_BUN_IF_NEEDED) {
            group = "bun"
            dependsOn(taskCheckIfBunInstalled)
            inputs.file(bunInstallationMetadataFile)
            doLast {
                def isBunInstalledOnSystem = inputs.files.singleFile.text
                if (isBunInstalledOnSystem == "YES") {
                    logger.info("Bun is already installed!")
                } else {
                    logger.info("Bun is NOT installed! Installing Bun...")
                    exec {
                        executable = "bash"
                        args("-c", "curl -fsSL https://bun.sh/install | bash && source \$HOME/.bashrc")
                    }
                }
            }
        }
    }
}