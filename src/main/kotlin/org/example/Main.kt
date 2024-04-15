package org.example

val sec = 1000L

fun main() {
    val executionManager = ExecutionManagerImpl()

    val task1 = Runnable {
        Thread.sleep(sec*5)
        println("#task1")
    }
    val task2 = Runnable {
        Thread.sleep(sec*8)
        println("#task2")
    }
    val task3 = Runnable {
        println("#task3")
    }
    val task4 = Runnable {
        throw Exception()
    }
    val callable = Runnable {
        println("#callable")
    }

    val context = executionManager.execute(callable, listOf(task1, task2, task3, task4))

    println("completed: ${context.getCompletedTaskCount()}")
    println("finished: ${context.getFailedTaskCount()}")
    println("isFinished: ${context.isFinished()}")
    println("failed: ${context.getFailedTaskCount()}")
    println("interrupted: ${context.getInterruptedTaskCount()}")
    Thread.sleep(sec*6)
    context.interrupt()
    println("-----------------------------------------------")
    println("completed: ${context.getCompletedTaskCount()}")
    println("finished: ${context.getFailedTaskCount()}")
    println("isFinished: ${context.isFinished()}")
    println("failed: ${context.getFailedTaskCount()}")
    println("interrupted: ${context.getInterruptedTaskCount()}")
}