package dev.jonrapp.hytaleReactiveUi.utils;

/**
 * Utility class for working with UI selectors and element paths.
 */
public class UiUtils {

    /**
     * Generates a selector for an array element at the specified index.
     * <p>
     * This is used when creating multiple instances of the same element type,
     * such as items in a list. The index is appended directly to the selector.
     *
     * @param selector the base selector for the array element
     * @param index the index of the element in the array
     * @return the selector with the index appended (e.g., "#Item" + 0 = "#Item0")
     */
    public static String getArraySelector(String selector, int index){
        return selector + index;
    }

    /**
     * Combines multiple selectors into a single selector path.
     * <p>
     * Selectors are joined with spaces to form a descendant selector path.
     * For example: {@code selectors("#Container", "#Item", ".Text")} returns
     * {@code "#Container #Item .Text"}.
     *
     * @param s the selectors to combine
     * @return the combined selector path with space separators
     */
    public static String selectors(String... s) {
        return String.join(" ", s);
    }

}
