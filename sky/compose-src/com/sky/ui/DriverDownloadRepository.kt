package com.sky.ui

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URI
import java.nio.channels.FileLock
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference

enum class DriverDownloadStatus { QUEUED, DOWNLOADING, VERIFYING, COMPLETED, CANCELLED, FAILED }
enum class DriverIntegrity { NOT_CHECKED, SIZE_ONLY, SHA256_VERIFIED }
data class DriverDownload(
    val entry: DriverEntry,
    val status: DriverDownloadStatus,
    val downloadedBytes: Long = 0,
    val integrity: DriverIntegrity = DriverIntegrity.NOT_CHECKED,
    val file: File? = null,
    val error: String? = null,
)

class DriverDownloadRepository(private val directory: File) {
    constructor(context: Context) : this(File(context.applicationContext.filesDir, "drivers"))

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val control = Mutex()
    private val transfers = Semaphore(2)
    private val manifestLock = Mutex()
    private val stateLock = Any()
    private val mutableDownloads = MutableStateFlow<List<DriverDownload>>(emptyList())
    val downloads: StateFlow<List<DriverDownload>> = mutableDownloads.asStateFlow()
    private val jobs = mutableMapOf<String, Job>()
    private val entries = linkedMapOf<String, DriverEntry>()
    private val connectionSlots = mutableMapOf<String, AtomicReference<HttpURLConnection?>>()
    private var lockFile: RandomAccessFile? = null
    private var directoryLock: FileLock? = null
    private var closed = false
    private val ready = scope.async { restore() }

    suspend fun awaitReady() { ready.await() }

    suspend fun download(entry: DriverEntry): Boolean = withContext(Dispatchers.IO) {
        validateDriverEntry(entry)
        ready.await()
        control.withLock {
            check(!closed) { "Repository is closed" }
            if (jobs[entry.id]?.isActive == true) return@withLock false
            val existing = entries[entry.id]
            require(existing == null || existing == entry) { "Delete the existing entry before replacing it" }
            if (downloads.value.any { it.entry.id == entry.id && it.status == DriverDownloadStatus.COMPLETED }) {
                return@withLock false
            }
            require(entries.size < 512 || existing != null) { "Too many downloads" }
            entries[entry.id] = entry
            try { saveManifest() } catch (failure: Exception) {
                if (existing == null) entries.remove(entry.id)
                throw failure
            }
            publish(DriverDownload(entry, DriverDownloadStatus.QUEUED))
            val slot = AtomicReference<HttpURLConnection?>()
            connectionSlots[entry.id] = slot
            jobs[entry.id] = scope.launch {
                try { transfers.withPermit { transfer(entry, slot) } }
                catch (cancelled: CancellationException) {
                    if (downloads.value.any { it.entry.id == entry.id && it.status == DriverDownloadStatus.QUEUED }) {
                        publish(DriverDownload(entry, DriverDownloadStatus.CANCELLED))
                    }
                    throw cancelled
                }
            }
            true
        }
    }

    suspend fun cancel(id: String): Unit = withContext(Dispatchers.IO) {
        ready.await()
        control.withLock {
            check(!closed)
            stop(id)
        }
    }

    suspend fun retry(id: String): Boolean {
        ready.await()
        val entry = control.withLock { check(!closed); entries[id] ?: throw IllegalArgumentException("Unknown driver") }
        return download(entry)
    }

    suspend fun delete(id: String): Unit = withContext(Dispatchers.IO) {
        ready.await()
        control.withLock {
            check(!closed)
            val entry = entries[id] ?: return@withLock
            stop(id)
            listOf(partFile(entry.id), finalFile(entry.id)).forEach {
                if (it.exists() && !it.delete()) throw IOException("Cannot delete driver file")
            }
            entries.remove(id)
            try { saveManifest() } catch (failure: Exception) { entries[id] = entry; throw failure }
            synchronized(stateLock) { mutableDownloads.value = mutableDownloads.value.filterNot { it.entry.id == id } }
            jobs.remove(id); connectionSlots.remove(id)
            Unit
        }
    }

    suspend fun close(): Unit = withContext(Dispatchers.IO) {
        try { ready.await() } catch (failure: Exception) { scope.cancel(); throw failure }
        control.withLock {
            if (!closed) {
                closed = true
                jobs.keys.toList().forEach { stop(it) }
                directoryLock?.release()
                lockFile?.close()
                scope.cancel()
            }
        }
    }

    private suspend fun stop(id: String) {
        val job = jobs[id]
        job?.cancel()
        connectionSlots[id]?.getAndSet(null)?.disconnect()
        job?.join()
        val state = downloads.value.firstOrNull { it.entry.id == id }
        if (state?.status == DriverDownloadStatus.QUEUED) {
            publish(state.copy(status = DriverDownloadStatus.CANCELLED))
        }
    }

    private fun finalFile(id: String) = File(directory, "$id.driver")
    private fun partFile(id: String) = File(directory, "$id.part")
    private fun publish(value: DriverDownload) = synchronized(stateLock) {
        mutableDownloads.value = mutableDownloads.value.filterNot { it.entry.id == value.entry.id } + value
    }

    private suspend fun restore() {
        require(directory.isDirectory || directory.mkdirs()) { "Cannot create driver directory" }
        val handle = RandomAccessFile(File(directory, ".sky-download.lock"), "rw")
        try {
            directoryLock = handle.channel.tryLock() ?: throw IOException("Driver directory is already in use")
            lockFile = handle
            val manifest = File(directory, "manifest.json")
            if (!manifest.exists()) return
            require(manifest.length() <= 2 * 1024 * 1024) { "Manifest too large" }
            val root = JSONObject(manifest.readText(Charsets.UTF_8))
            require(root.strictLong("version") == 1L) { "Unsupported manifest" }
            val array = root.getJSONArray("drivers")
            require(array.length() <= 512)
            val restored = (0 until array.length()).map { driverEntryFromJson(array.getJSONObject(it)) }
            require(restored.map { it.id }.toSet().size == restored.size)
            restored.forEach { entry ->
                currentCoroutineContext().ensureActive()
                entries[entry.id] = entry
                val file = finalFile(entry.id)
                if (!file.exists()) {
                    publish(DriverDownload(entry, DriverDownloadStatus.CANCELLED, error = "下载未完成，请重试"))
                } else {
                    try {
                        val integrity = verify(file, entry)
                        publish(DriverDownload(entry, DriverDownloadStatus.COMPLETED, entry.size, integrity, file))
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) {
                        publish(DriverDownload(entry, DriverDownloadStatus.FAILED, error = "本地文件校验失败，请重试"))
                    }
                }
            }
        } catch (failure: Exception) {
            directoryLock?.release(); directoryLock = null
            handle.close(); lockFile = null
            throw failure
        }
    }

    private suspend fun saveManifest() = manifestLock.withLock {
        val root = JSONObject().put("version", 1).put("drivers", JSONArray(entries.values.map { it.toManifestJson() }))
        val temporary = File(directory, "manifest.json.part")
        FileOutputStream(temporary).use { output ->
            output.write(root.toString().toByteArray(Charsets.UTF_8)); output.fd.sync()
        }
        Files.move(temporary.toPath(), File(directory, "manifest.json").toPath(),
            StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        Unit
    }

    private suspend fun transfer(entry: DriverEntry, slot: AtomicReference<HttpURLConnection?>) {
        var count = 0L
        try {
            if (directory.usableSpace < entry.size + 1024 * 1024) throw IOException("Insufficient disk space")
            publish(DriverDownload(entry, DriverDownloadStatus.DOWNLOADING))
            readResponse(entry, slot) { connection ->
                val length = connection.contentLengthLong
                if (length >= 0 && length != entry.size) throw IOException("Response size mismatch")
                val encoding = connection.getHeaderField("Content-Encoding")
                if (encoding != null && !encoding.equals("identity", true)) throw IOException("Unexpected encoding")
                connection.inputStream.use { input ->
                    FileOutputStream(partFile(entry.id), false).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var lastProgress = 0L
                        val deadline = System.nanoTime() + 30L * 60 * 1_000_000_000
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            if (System.nanoTime() > deadline) throw IOException("Download time limit exceeded")
                            val read = input.read(buffer)
                            if (read == -1) break
                            if (read.toLong() > entry.size - count) throw IOException("Response exceeds declared size")
                            if (directory.usableSpace < read + 1024L * 1024) throw IOException("Insufficient disk space")
                            output.write(buffer, 0, read)
                            count += read
                            if (count - lastProgress >= 256 * 1024 || count == entry.size) {
                                publish(DriverDownload(entry, DriverDownloadStatus.DOWNLOADING, count))
                                lastProgress = count
                            }
                        }
                        if (count != entry.size) throw IOException("Incomplete response")
                        output.fd.sync()
                    }
                }
            }
            publish(DriverDownload(entry, DriverDownloadStatus.VERIFYING, count))
            val integrity = verify(partFile(entry.id), entry)
            currentCoroutineContext().ensureActive()
            Files.move(partFile(entry.id).toPath(), finalFile(entry.id).toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            publish(DriverDownload(entry, DriverDownloadStatus.COMPLETED, count, integrity, finalFile(entry.id)))
        } catch (cancelled: CancellationException) {
            publish(DriverDownload(entry, DriverDownloadStatus.CANCELLED, count))
            throw cancelled
        } catch (_: Exception) {
            val cancelled = !currentCoroutineContext().isActive
            publish(DriverDownload(entry,
                if (cancelled) DriverDownloadStatus.CANCELLED else DriverDownloadStatus.FAILED,
                count, error = if (cancelled) null else "下载或校验失败，请检查网络、空间及远程文件后重试"))
        } finally {
            slot.getAndSet(null)?.disconnect()
        }
    }

    private suspend fun readResponse(
        entry: DriverEntry,
        slot: AtomicReference<HttpURLConnection?>,
        consume: suspend (HttpURLConnection) -> Unit,
    ) = coroutineScope {
        var url = entry.url
        for (hop in 0..5) {
            currentCoroutineContext().ensureActive()
            requireHttpsUrl(url)
            val connection = URI(url).toURL().openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept-Encoding", "identity")
            slot.set(connection)
            // Run the cancellation watcher on another IO worker while this worker blocks in read().
            val disconnectOnCancel = launch(Dispatchers.IO, start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
                try { awaitCancellation() } finally { connection.disconnect() }
            }
            try {
                val status = connection.responseCode
                if (status in setOf(301, 302, 303, 307, 308)) {
                    if (hop == 5) throw IOException("Too many redirects")
                    val location = connection.getHeaderField("Location") ?: throw IOException("Missing redirect")
                    url = requireHttpsUrl(URI(url).resolve(location).toString())
                } else {
                    if (status != 200) throw IOException("HTTP $status")
                    consume(connection)
                    return@coroutineScope
                }
            } finally {
                disconnectOnCancel.cancelAndJoin()
                slot.compareAndSet(connection, null)
                connection.disconnect()
            }
        }
    }

    private suspend fun verify(file: File, entry: DriverEntry): DriverIntegrity {
        if (!file.isFile || file.length() != entry.size || Files.isSymbolicLink(file.toPath())) {
            throw IOException("File size mismatch")
        }
        currentCoroutineContext().ensureActive()
        val expected = entry.sha256 ?: return DriverIntegrity.SIZE_ONLY
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            var total = 0L
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = input.read(buffer)
                if (count == -1) break
                total += count
                if (total > entry.size) throw IOException("File changed")
                digest.update(buffer, 0, count)
            }
            if (total != entry.size) throw IOException("File changed")
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        if (!actual.equals(expected, true)) throw IOException("SHA256 mismatch")
        return DriverIntegrity.SHA256_VERIFIED
    }
}

