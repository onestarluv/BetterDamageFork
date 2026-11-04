package kr.toxicity.damage.api.manager;

import kr.toxicity.damage.api.database.DamagePlayerDatabase;
import org.jetbrains.annotations.NotNull;

/**
 * Database manager
 */
public interface DatabaseManager extends DamageManager {
    /**
     * Gets current connection
     * @return current connection
     */
    @NotNull DamagePlayerDatabase.Connection connection();

}
