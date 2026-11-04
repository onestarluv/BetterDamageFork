package kr.toxicity.damage.compatibility.nexo

import java.util.concurrent.CompletableFuture
import kotlin.test.*

class CouponTransactionTest {
    @Test
    fun `coupon cannot be consumed until permissions are saved and consumes only once`() {
        val save = CompletableFuture<Void>()
        var consumed = 0
        var rollbacks = 0
        val transaction = CouponTransaction(save) {
            rollbacks++
            CompletableFuture.completedFuture(null)
        }
        assertFalse(transaction.consume { consumed++; true })
        assertEquals(0, consumed)
        save.complete(null)
        assertTrue(transaction.consume { consumed++; true })
        assertFalse(transaction.consume { consumed++; true })
        transaction.cancel()
        assertEquals(1, consumed)
        assertEquals(0, rollbacks)
        assertTrue(transaction.completion.join())
    }

    @Test
    fun `disconnect during save undoes permission after original save and never consumes`() {
        val save = CompletableFuture<Void>()
        val undo = CompletableFuture<Void>()
        var rollbacks = 0
        val transaction = CouponTransaction(save) { rollbacks++; undo }
        transaction.cancel()
        assertEquals(0, rollbacks)
        save.complete(null)
        assertEquals(1, rollbacks)
        assertFalse(transaction.consume { fail("Disconnected player's coupon must remain untouched") })
        assertFalse(transaction.completion.isDone)
        undo.complete(null)
        assertFalse(transaction.completion.join())
    }

    @Test
    fun `moved coupon rolls back saved permission without consuming any item`() {
        val save = CompletableFuture.completedFuture<Void>(null)
        var rollbacks = 0
        val transaction = CouponTransaction(save) { rollbacks++; CompletableFuture.completedFuture(null) }
        assertFalse(transaction.consume { false })
        assertEquals(1, rollbacks)
        assertFalse(transaction.completion.join())
    }

    @Test
    fun `failed permission storage never consumes and allows rollback`() {
        val save = CompletableFuture<Void>()
        var rollbacks = 0
        val transaction = CouponTransaction(save) { rollbacks++; CompletableFuture.completedFuture(null) }
        save.completeExceptionally(IllegalStateException("storage unavailable"))
        assertFalse(transaction.consume { fail("Failed storage must not consume a coupon") })
        transaction.cancel()
        transaction.cancel()
        assertEquals(1, rollbacks)
        assertFalse(transaction.completion.join())
    }
}
