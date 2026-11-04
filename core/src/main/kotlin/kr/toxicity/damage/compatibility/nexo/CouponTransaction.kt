package kr.toxicity.damage.compatibility.nexo

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

/** The permission save must complete before an inventory item can be consumed. */
internal class CouponTransaction(
    val saved: CompletableFuture<Void>,
    private val rollback: () -> CompletableFuture<Void>
) {
    private val settled = AtomicBoolean()
    val completion = CompletableFuture<Boolean>()

    fun consume(action: () -> Boolean): Boolean {
        if (!saved.isDone || saved.isCompletedExceptionally || saved.isCancelled) return false
        if (!settled.compareAndSet(false, true)) return false
        return try {
            action().also { if (it) completion.complete(true) else undo() }
        } catch (error: Throwable) {
            undo()
            throw error
        }
    }

    fun cancel() {
        if (settled.compareAndSet(false, true)) undo()
    }

    private fun undo() {
        // Sequence after the original write, including when a quit/shutdown races with it.
        saved.handle { _, _ -> null }.thenCompose { rollback() }.whenComplete { _, error ->
            if (error == null) completion.complete(false) else completion.completeExceptionally(error)
        }
    }
}
