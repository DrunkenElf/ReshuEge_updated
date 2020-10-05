package com.ilnur.utils

import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream
import java.util.stream.IntStream

internal class StreamReader {



    internal fun readLines(bufReader: BufferedReader): String? {
        val sBuffer = StringBuffer()
        var line: String?
        while (true) {
            line = bufReader.readLine()
            if (line == null)
                break
            sBuffer.append(line + "\n")
        }
        bufReader.close()
        return sBuffer.toString()
    }

    internal fun writeFile(input: InputStream, output: FileOutputStream)  {
        val data = ByteArray(4096)
        var count: Int
        while (true) {
            count = input.read(data)
            if (count == -1)
                break
            output.write(data, 0, count)
        }
        output.flush()
        output.close()
        input.close()
    }
}