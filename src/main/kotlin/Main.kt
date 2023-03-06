package cryptography

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

val menuMap = mutableMapOf("hide" to { hide() }, "show" to { show() })
fun input(prompt: String) = println(prompt).run { readln().trim() }
fun checkF(f: String) = try {
    ImageIO.read(File(f))
} catch (e: Exception) {
    println("Can't read input file!"); null
}

fun Byte.toBits(): List<Int> {
    return List(8) { (this.toInt() ushr (7 - it)) and 1 }
}

fun xorString(str1: String, pass: String) = str1.zip(pass.repeat((str1.length + pass.length - 1) / pass.length))
    .map { (a, b) -> a.code xor b.code }.map(Int::toChar).joinToString("")

fun show() {
    val inputFile = input("Input image file:")
    val image = checkF(inputFile) ?: return
    val password = input("Password:")
    val strArray = image.getRGB(0, 0, image.width, image.height, null, 0, image.width).map { it and 1 }.chunked(8)
        .map { it.reduce { byte, i -> byte shl 1 or i } }
    val str = strArray.subList(0, strArray.windowed(3, 1).indexOfFirst { it == listOf(0, 0, 3) })
        .map { it.toByte() }.toByteArray().toString(Charsets.UTF_8)
    xorString(str, password).let { println("Message:\n$it") }
}

fun hide() {
    val (inputFile, outputFile) = listOf(input("Input image file:"), input("Output image file:"))
    val image = checkF(inputFile) ?: return
    val (message, password) = listOf(input("Message to hide:"), input("Password:"))
    val messageBytes = xorString(message, password).toByteArray(Charsets.UTF_8) + byteArrayOf(0, 0, 3)
    if (messageBytes.size * 8 > image.width * image.height) {
        println("The input image is not large enough to hold this message.").also { return }
    }
    val bits = messageBytes.flatMap { it.toBits() }
    var messIndex = 0
    image.apply {
        (0 until height).flatMap { y ->
            (0 until width).map { x ->
                if (messIndex < messageBytes.size * 8) {
                    val (r, g, b) = with(Color(getRGB(x, y))) { listOf(red, green, blue) }
                    setRGB(x, y, Color(r, g, b.and(254).or(if (messIndex < bits.size) bits[messIndex++] else 0)).rgb)
                }
            }
        }
    }
    ImageIO.write(image, "png", File(outputFile)).also { println("Message saved in $outputFile image.") }
}

fun start() {
    while (true) {
        when (val task = input("Task (hide, show, exit):")) {
            "exit" -> break
            "hide", "show" -> menuMap[task]?.invoke()
            else -> println("Wrong task: $task")
        }
    }
}

fun main() = start().also { println("Bye!") }