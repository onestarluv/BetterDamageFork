package kr.toxicity.damage.api.database;

import kr.toxicity.damage.api.player.DamagePlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Player database
 */
public interface DamagePlayerDatabase {

    /**
     * Kokonut-Core의 데이터베이스 서비스에 연결합니다.
     * @return connection
     */
    @NotNull Connection connect();

    /**
     * Connection
     */
    interface Connection extends AutoCloseable {
        /**
         * Loads player data
         * @param uuid player's uuid
         * @return player data
         * @throws Exception connection exception
         */
        @NotNull DamagePlayerData load(@NotNull UUID uuid) throws Exception;

        /**
         * Saves player data
         * @param uuid player's uuid
         * @param data player data
         * @throws Exception connection exception
         */
        void save(@NotNull UUID uuid, @NotNull DamagePlayerData data) throws Exception;
    }
}
