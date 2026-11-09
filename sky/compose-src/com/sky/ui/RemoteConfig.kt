package com.sky.ui

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

const val MAX_DRIVER_SIZE: Long = 1024L * 1024 * 1024

data class MaintenanceConfig(val enabled: Boolean = false, val message: String = "")
data class RemoteLinks(
    val support: String? = null,
    val about: String? = null,
    val source: String? = null,
    val community: String? = null,
)
data class DriverEntry(
    val id: String,
    val name: String,
    val url: String,
    val size: Long,
    val sha256: String? = null,
    val abis: Set<String>? = null,
    val minApi: Int? = null,
    val maxApi: Int? = null,
    val kernelPrefix: String? = null,
    val cardTypes: Set<String>? = null,
)
data class DriverDevice(val abis: Set<String>, val api: Int, val kernel: String)
data class SkyRemoteConfig(
    val maintenance: MaintenanceConfig = MaintenanceConfig(),
    val features: Map<String, Boolean> = emptyMap(),
    val links: RemoteLinks = RemoteLinks(),
    val drivers: List<DriverEntry> = emptyList(),
) {
    fun featureEnabled(name: String): Boolean = features[name] == true
    fun compatibleDrivers(device: DriverDevice, cardType: String?): List<DriverEntry> =
        drivers.filter { isDriverCompatible(it, device, cardType) }
}

fun requireHttpsUrl(value: String): String {
    val uri = URI(value)
    require(uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrEmpty() &&
        uri.rawUserInfo == null && uri.rawFragment == null && (uri.port == -1 || uri.port in 1..65535)) {
        "URL must be HTTPS without credentials or fragment"
    }
    return value
}

fun validateDriverEntry(entry: DriverEntry): DriverEntry {
    require(entry.id.matches(Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,63}"))) { "Invalid driver id" }
    require(entry.name.isNotBlank() && entry.name.length <= 256) { "Invalid driver name" }
    requireHttpsUrl(entry.url)
    require(entry.size in 1..MAX_DRIVER_SIZE) { "Invalid driver size" }
    require(entry.sha256 == null || entry.sha256.matches(Regex("[a-fA-F0-9]{64}"))) { "Invalid SHA256" }
    require(entry.minApi == null || entry.minApi >= 1)
    require(entry.maxApi == null || entry.maxApi >= 1)
    require(entry.minApi == null || entry.maxApi == null || entry.minApi <= entry.maxApi)
    require(entry.kernelPrefix == null || entry.kernelPrefix.isNotBlank())
    listOf(entry.abis, entry.cardTypes).forEach { values ->
        require(values == null || (values.isNotEmpty() && values.size <= 128 &&
            values.all { it.isNotBlank() && it.length <= 128 })) { "Invalid filter" }
    }
    return entry
}

fun isDriverCompatible(entry: DriverEntry, device: DriverDevice, cardType: String?): Boolean =
    (entry.abis == null || entry.abis.any { it in device.abis }) &&
        (entry.minApi == null || device.api >= entry.minApi) &&
        (entry.maxApi == null || device.api <= entry.maxApi) &&
        (entry.kernelPrefix == null || device.kernel.startsWith(entry.kernelPrefix)) &&
        (entry.cardTypes == null || (cardType != null && cardType in entry.cardTypes))

fun parseRemoteConfig(json: String): SkyRemoteConfig {
    require(json.toByteArray(Charsets.UTF_8).size <= 2 * 1024 * 1024) { "Config too large" }
    val root = JSONObject(json)
    val maintenance = root.optionalObject("maintenance")
    val featureObject = root.optionalObject("features")
    val links = root.optionalObject("links")
    val drivers = if (root.has("drivers")) root.get("drivers") as? JSONArray
        ?: throw IllegalArgumentException("drivers must be an array") else JSONArray()
    require(drivers.length() <= 512) { "Too many drivers" }
    val entries = (0 until drivers.length()).map { driverEntryFromJson(drivers.getJSONObject(it)) }
    require(entries.map { it.id }.toSet().size == entries.size) { "Duplicate driver id" }
    return SkyRemoteConfig(
        maintenance = MaintenanceConfig(
            maintenance?.strictBoolean("enabled", false) ?: false,
            maintenance?.optionalString("message") ?: "",
        ),
        features = featureObject?.let { obj -> obj.keys().asSequence().associateWith { obj.strictBoolean(it) } }
            ?: emptyMap(),
        links = RemoteLinks(
            links?.optionalString("support")?.let(::requireHttpsUrl),
            links?.optionalString("about")?.let(::requireHttpsUrl),
            links?.optionalString("source")?.let(::requireHttpsUrl),
            links?.optionalString("community")?.let(::requireHttpsUrl),
        ),
        drivers = entries,
    )
}

internal fun driverEntryFromJson(obj: JSONObject): DriverEntry = validateDriverEntry(DriverEntry(
    id = obj.requiredString("id"), name = obj.requiredString("name"), url = obj.requiredString("url"),
    size = obj.strictLong("size"), sha256 = obj.optionalString("sha256"),
    abis = obj.optionalStrings("abis"), minApi = obj.optionalApi("minApi"),
    maxApi = obj.optionalApi("maxApi"), kernelPrefix = obj.optionalString("kernelPrefix"),
    cardTypes = obj.optionalStrings("cardTypes"),
))

internal fun DriverEntry.toManifestJson(): JSONObject = JSONObject().apply {
    put("id", id); put("name", name); put("url", url); put("size", size)
    sha256?.let { put("sha256", it) }; abis?.let { put("abis", JSONArray(it.toList())) }
    minApi?.let { put("minApi", it) }; maxApi?.let { put("maxApi", it) }
    kernelPrefix?.let { put("kernelPrefix", it) }
    cardTypes?.let { put("cardTypes", JSONArray(it.toList())) }
}

private fun JSONObject.optionalObject(key: String): JSONObject? =
    if (!has(key)) null else get(key) as? JSONObject ?: throw IllegalArgumentException("$key must be an object")
private fun JSONObject.requiredString(key: String): String =
    get(key) as? String ?: throw IllegalArgumentException("$key must be a string")
private fun JSONObject.optionalString(key: String): String? = if (!has(key)) null else requiredString(key)
private fun JSONObject.strictBoolean(key: String, default: Boolean? = null): Boolean =
    if (!has(key) && default != null) default else get(key) as? Boolean
        ?: throw IllegalArgumentException("$key must be boolean")
internal fun JSONObject.strictLong(key: String): Long {
    val value = get(key)
    require(value is Int || value is Long) { "$key must be an integer" }
    return (value as Number).toLong()
}
private fun JSONObject.optionalApi(key: String): Int? = if (!has(key)) null else strictLong(key).let {
    require(it in 1..Int.MAX_VALUE.toLong()) { "Invalid API level" }; it.toInt()
}
private fun JSONObject.optionalStrings(key: String): Set<String>? {
    if (!has(key)) return null
    val array = get(key) as? JSONArray ?: throw IllegalArgumentException("$key must be an array")
    require(array.length() in 1..128)
    return (0 until array.length()).map { array.get(it) as? String
        ?: throw IllegalArgumentException("$key must contain strings") }.toSet()
}
