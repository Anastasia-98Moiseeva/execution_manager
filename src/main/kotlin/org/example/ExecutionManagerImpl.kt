package org.example

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.allOf
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors.newFixedThreadPool

class ExecutionManagerImpl : ExecutionManager {
    private val threadNumber = 2
    private val threadFactory = ThreadFactoryBuilder()
        .setNameFormat("Task-%d")
        .build()

    private val executor = newFixedThreadPool(threadNumber, threadFactory)

    override fun execute(callback: Runnable, tasks: List<Runnable>): Context {
        val tasksById = ConcurrentHashMap(
            tasks.indices
                .associateWith { Task(tasks[it], false) }
                .toMutableMap()
        )
        val futuresByTaskId = tasksById.mapValues { (id, task) ->
            val wrappedTask = Runnable {
                run {
                    tasksById[id] = task.copy(isStarted = true)
                    Thread.sleep(2000)
                    task.task.run()
                    tasksById[id] = task.copy(isStarted = false)
                }
            }
            runAsync(wrappedTask, executor)
        }
        allOf(*futuresByTaskId.values.toTypedArray()).thenRun(callback)
        return ContextImpl(tasksById, futuresByTaskId)
    }

    private class ContextImpl(
        private val tasksById: ConcurrentHashMap<Int, Task>,
        private val futuresByTaskId: Map<Int, CompletableFuture<Void>>,
    ) : Context {
        override fun getCompletedTaskCount(): Int = futuresByTaskId.values.count {
            it.isDone && !it.isCompletedExceptionally && !it.isCancelled
        }

        override fun getFailedTaskCount(): Int = futuresByTaskId.values.count {
            it.isCompletedExceptionally && !it.isCancelled
        }

        override fun getInterruptedTaskCount(): Int = futuresByTaskId.values.count { it.isCancelled }

        override fun getFinishedTaskCount(): Int = futuresByTaskId.values.count { it.isDone }

        override fun interrupt() = tasksById
            .filterNot { (_, task) -> task.isStarted }
            .keys
            .forEach {
                futuresByTaskId[it]?.cancel(false)
            }

        override fun isFinished(): Boolean = getFinishedTaskCount() == futuresByTaskId.size
    }

    private data class Task(
        val task: Runnable,
        val isStarted: Boolean,
    )
}