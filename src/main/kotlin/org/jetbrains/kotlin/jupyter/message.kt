package org.jetbrains.kotlin.jupyter

import com.beust.klaxon.JsonObject
import java.text.SimpleDateFormat
import java.util.*

private val ISO8601DateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
internal val ISO8601DateNow: String get() = ISO8601DateFormatter.format(Date())

internal val emptyJsonObject = JsonObject()
internal val emptyJsonObjectString = emptyJsonObject.toJsonString(prettyPrint = false)
internal val emptyJsonObjectStringBytes = emptyJsonObjectString.toByteArray()

data class Message(
        val id: List<ByteArray> = listOf(),
        val header: JsonObject? = null,
        val parentHeader: JsonObject? = null,
        val metadata: JsonObject? = null,
        val content: JsonObject? = null,
        val blob: ByteArray? = null
) {
    override fun toString(): String =
            "msg[${id.joinToString { it.toString(charset = Charsets.UTF_8) }}]" +
            " header = ${header?.toJsonString(false) ?: emptyJsonObjectString}" +
            " parentHeader = ${parentHeader?.toJsonString(false) ?: emptyJsonObjectString}" +
            " metadata = ${metadata?.toJsonString(false) ?: emptyJsonObjectString}" +
            " content = ${content?.toJsonString(false) ?: emptyJsonObjectString}"
}

fun jsonObject(vararg namedVals: Pair<String, Any?>): JsonObject = JsonObject(hashMapOf(*namedVals))
fun jsonObject(namedVals: Iterable<Pair<String, Any?>>): JsonObject = JsonObject(HashMap<String, Any?>().apply { putAll(namedVals) })

internal operator fun JsonObject?.get(key: String) = this?.get(key)

fun makeReplyMessage(msg: Message,
                     msgType: String? = null,
                     sessionId: String? = null,
                     header: JsonObject? = null,
                     parentHeader: JsonObject? = null,
                     metadata: JsonObject? = null,
                     content: JsonObject? = null) =
        Message(id = msg.id,
                header = header ?: makeHeader(msgType = msgType, incomingMsg = msg, sessionId = sessionId),
                parentHeader = parentHeader ?: msg.header,
                metadata = metadata,
                content = content)

fun makeHeader(msgType: String? = null, incomingMsg: Message? = null, sessionId: String? = null): JsonObject = jsonObject(
        "msg_id" to UUID.randomUUID().toString(),
        "date" to ISO8601DateNow,
        "version" to protocolVersion,
        "username" to ((incomingMsg?.header?.get("username") as? String) ?: "kernel"),
        "session" to ((incomingMsg?.header?.get("session") as? String) ?: sessionId),
        "msg_type" to (msgType ?: "none"))