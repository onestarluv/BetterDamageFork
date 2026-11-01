package kr.toxicity.damage.api.nms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents server's version.
 */
@RequiredArgsConstructor
@Getter
public enum NMSVersion {
    /**
     * 26.3
     */
    V26_R3(26,3, 97)
    ;
    /**
     * Main version.
     */
    private final int version;
    /**
     * Sub version.
     */
    private final int subVersion;
    /**
     * That client version's resource pack mcmeta version.
     */
    private final int metaVersion;
}