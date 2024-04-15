package org.example

interface ExecutionManager {
    fun execute(callback: Runnable, tasks: List<Runnable>): Context
}