package kr.toxicity.damage.compatibility.nexo

import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Each dialog answer belongs to a single player and the most recently opened request. */
internal class CouponConfirmationRequests(private val now: () -> Long = System::nanoTime) {
    private data class Request(val token: UUID, val expires: Long)
    private val requests = ConcurrentHashMap<UUID, Request>()

    fun open(player: UUID): UUID = UUID.randomUUID().also {
        requests[player] = Request(it, now() + Duration.ofMinutes(2).toNanos())
    }

    fun answer(player: UUID, token: UUID): Boolean {
        val request = requests[player] ?: return false
        if (request.token != token || !requests.remove(player, request)) return false
        return now() < request.expires
    }

    fun cancel(player: UUID) { requests.remove(player) }
    fun clear() { requests.clear() }
}
