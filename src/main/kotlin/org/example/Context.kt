package org.example

interface Context {
    fun getCompletedTaskCount(): Int
    fun getFailedTaskCount(): Int
    fun getInterruptedTaskCount(): Int
    fun getFinishedTaskCount(): Int
    fun interrupt()
    fun isFinished(): Boolean
}