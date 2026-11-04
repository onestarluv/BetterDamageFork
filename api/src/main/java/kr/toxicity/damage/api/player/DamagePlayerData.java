package kr.toxicity.damage.api.player;

import kr.toxicity.damage.api.BetterDamage;
import kr.toxicity.damage.api.effect.DamageEffect;
import kr.toxicity.damage.api.skin.DamageSkin;
import kr.toxicity.damage.api.trigger.DamageTriggerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.UUID;

/**
 * Data of player
 */
public interface DamagePlayerData {
    /**
     * 현재 권한이 유효한 장착 스킨을 반환합니다.
     * @return 장착 스킨. 권한이 없거나 미접속이면 null
     */
    @Nullable DamageSkin selectedSkin();

    /**
     * 플레이어의 현재 권한으로 사용 가능한 스킨을 반환합니다.
     * @return 사용 가능한 스킨 목록. 미접속이면 빈 목록
     */
    @NotNull @Unmodifiable
    Collection<DamageSkin> skins();

    /**
     * 해당 스킨 권한을 확인하고 장착합니다.
     * @param skin 장착할 스킨
     * @return 권한이 있어 장착했으면 true
     */
    boolean select(@NotNull DamageSkin skin);

    /** 장착을 해제하고 공통 기본 스킨/효과로 돌아갑니다. */
    void clearSelection();

    /**
     * Saves this data to some player's uuid
     * @param uuid player's uuid
     * @throws Exception connection exception
     */
    default void save(@NotNull UUID uuid) throws Exception {
        BetterDamage.inst().databaseManager().connection().save(uuid, this);
    }

    /**
     * Gets a proper effect by given type
     * @param type type
     * @return effect or null
     */
    default @Nullable DamageEffect effect(@NotNull DamageTriggerType type) {
        var inst = BetterDamage.inst();
        var selected = selectedSkin();
        if (selected == null) selected = inst.config().defaultSkin();
        if (selected == null) return inst.config().defaultEffect();
        else return selected.find(type);
    }
}
