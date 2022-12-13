package me.deotime.syncd.utils

import java.util.Base64

private val Encoder = Base64.getEncoder()
private val Decoder = Base64.getDecoder()

private val String2B64 = cache<String, String> { Encoder.encodeToString(it.toByteArray()) }
private val B642String = cache<String, String> { String(Decoder.decode(it)) }

fun String.toBase64() = String2B64[this]
fun String.base64ToString() = B642String[this]