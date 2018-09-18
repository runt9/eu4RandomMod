package com.runt9.eu4.randomizer.writer

import java.io.File

abstract class BaseWriter<T>(fileName: String) {
    private val file = File(fileName)

    protected fun writeLn(line: String) {
        file.appendText("$line\n", Charsets.ISO_8859_1)
    }

    abstract fun writeObj(obj: T)
}