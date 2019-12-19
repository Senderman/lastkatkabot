package com.senderman

abstract class AbstractExecutorKeeper {
    protected val commandExecutors: MutableMap<String, CommandExecutor> = HashMap()

    protected fun register(executor: CommandExecutor) {
        commandExecutors[executor.command] = executor
    }

    protected abstract fun registerCommands()

    fun findExecutor(command: String): CommandExecutor? = commandExecutors[command]
}