package org.mesdag.confluence_dimension_patch.mixed;

public interface IDimensionAccessor {
    void cdp$setIsOtherworld();

    boolean cdp$isOtherworld();

    static IDimensionAccessor of(Object o) {
        return (IDimensionAccessor) o;
    }
}
