package kr.toxicity.damage.compatibility.nexo

import java.time.Duration
import java.util.UUID
import kotlin.test.*

class CouponConfirmationRequestsTest {
    @Test
    fun `answer is bound to player and can only be used once`() {
        val requests = CouponConfirmationRequests()
        val player = UUID.randomUUID()
        val token = requests.open(player)
        assertFalse(requests.answer(UUID.randomUUID(), token))
        assertTrue(requests.answer(player, token))
        assertFalse(requests.answer(player, token))
    }

    @Test
    fun `reopening replaces previous dialog and cancel or logout invalidates confirmation`() {
        val requests = CouponConfirmationRequests()
        val player = UUID.randomUUID()
        val old = requests.open(player)
        val current = requests.open(player)
        assertFalse(requests.answer(player, old))
        // The cancel button claims the same one-shot answer without invoking redemption.
        assertTrue(requests.answer(player, current))
        assertFalse(requests.answer(player, current))
        val logout = requests.open(player)
        requests.cancel(player)
        assertFalse(requests.answer(player, logout))
    }

    @Test
    fun `expired dialogs and plugin shutdown cannot authorize a coupon`() {
        var now = 0L
        val requests = CouponConfirmationRequests { now }
        val player = UUID.randomUUID()
        val expired = requests.open(player)
        now = Duration.ofMinutes(2).toNanos()
        assertFalse(requests.answer(player, expired))
        val shutdown = requests.open(player)
        requests.clear()
        assertFalse(requests.answer(player, shutdown))
    }
}
