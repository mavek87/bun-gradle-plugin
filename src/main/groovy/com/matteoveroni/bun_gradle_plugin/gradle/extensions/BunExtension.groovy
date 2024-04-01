package com.matteoveroni.bun_gradle_plugin.gradle.extensions

import groovy.util.logging.Slf4j
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

import static com.matteoveroni.bun_gradle_plugin.gradle.plugins.BunFrontendPlugin.TASK_NAME_INSTALL_BUN_IF_NEEDED

// https://mbonnin.hashnode.dev/my-life-after-afterevaluate

@Slf4j
class BunExtension {

    private static final Set<String> DEFAULT_COMMANDS = [
            "install", "update", "upgrade"
    ]

    class BunOptions {
        Set<String> commands = DEFAULT_COMMANDS
    }

    Project project

    BunExtension(Project project) {
        this.project = project
    }

    void package_json(Action<BunOptions> action) {
        BunOptions options = new BunOptions()
        action.execute(options)

        def bunCommands = DEFAULT_COMMANDS + options.commands
        log.info("bunCommands: ${bunCommands}")

        for (String bunCommand : bunCommands) {
            executeBunCommand(bunCommand)
        }
    }

    void executeBunCommand(String bunCommand) {
        if (!bunCommand?.trim()?.startsWith("bun ")) {
            bunCommand = "bun ${bunCommand}"
        }
        log.info("bunCommand: ${bunCommand}")

        // The task name must not contain any of the following characters: [/, \, :, <, >, ", ?, *, |].
        def bunCommandName = bunCommand?.replaceAll(" ", "_")
        bunCommandName = bunCommandName?.replaceAll("/", "")
        bunCommandName = bunCommandName?.replaceAll("\\\\", "")
        bunCommandName = bunCommandName?.replaceAll(":", "-")
        bunCommandName = bunCommandName?.replaceAll("<", "")
        bunCommandName = bunCommandName?.replaceAll(">", "")
        bunCommandName = bunCommandName?.replaceAll("\"", "")
        bunCommandName = bunCommandName?.replaceAll("\\?", "")
        bunCommandName = bunCommandName?.replaceAll("\\*", "")
        bunCommandName = bunCommandName?.replaceAll("\\|", "")
        log.info("bunCommandName: ${bunCommandName}")

        project.tasks.register(bunCommandName, Exec) { it ->
            it.group = "bun"
            it.description = "${bunCommand}"
            it.commandLine "bash", "-ci", "${bunCommand}"
            it.standardOutput = new ByteArrayOutputStream()
            it.errorOutput = new ByteArrayOutputStream()
            it.dependsOn(project.tasks.named(TASK_NAME_INSTALL_BUN_IF_NEEDED))
            it.doLast {
                println "${bunCommand}"
                println "${standardOutput.toString()}"
            }
        }
    }
}
