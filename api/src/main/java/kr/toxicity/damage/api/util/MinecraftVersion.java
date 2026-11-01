package kr.toxicity.damage.api.util;

import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

import java.util.Comparator;
import java.util.Objects;

/**
 * Minecraft version.
 * @param first title
 * @param second main update
 * @param third minor update
 */
public record MinecraftVersion(int first, int second, int third) implements Comparable<MinecraftVersion> {
    /**
     * Comparator
     */
    private static final Comparator<MinecraftVersion> COMPARATOR = Comparator.comparing(MinecraftVersion::first)
            .thenComparing(MinecraftVersion::second)
            .thenComparing(MinecraftVersion::third);

    @Override
    public int compareTo(@NotNull MinecraftVersion o) {
        return COMPARATOR.compare(this, o);
    }

    /**
     * 26.3
     */
    public static final MinecraftVersion V26_3 = new MinecraftVersion(26, 3, 0);
    /**
     * Returns the current version supports text shadow color
     * @return support shadow color
     */
    public boolean supportShadowColor() {
        return true;
    }

    /**
     * Checks this version is greater than another one
     * @param other other
     * @return greater or not
     */
    public boolean isGreaterThan(@NotNull MinecraftVersion other) {
        return compareTo(other) >= 0;
    }

    /**
     * Parses version from string
     * @param version version like "26.3"
     */
    public MinecraftVersion(@NotNull String version) {
        var semver = Objects.requireNonNull(Semver.coerce(version));
        this(semver.getMajor(), semver.getMinor(), semver.getPatch());
    }

    @Override
    public @NotNull String toString() {
        return first + "." + second + "." + third;
    }
}