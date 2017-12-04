/*
 * ------------------------------------------------------------------------------
 * Gregor Santner <gsantner.net> & Lonami Exo <lonamiwebs.github.io> wrote
 * this. You can do whatever you want with it. If we meet some day, and you
 * think it is worth it, you can buy us a coke in return. Provided as is without
 * any kind of warranty. Do not blame or sue us if something goes wrong.
 * No attribution required.    - Gregor Santner & Lonami Exo
 *
 * License: Creative Commons Zero (CC0 1.0)
 *  http://creativecommons.org/publicdomain/zero/1.0/
 * ----------------------------------------------------------------------------
 */
package net.gsantner.opoc.util;

import java.util.regex.Pattern;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue", "SpellCheckingInspection", "deprecation"})
public class GeneralUtils {
    public static String toTitleCase(final String string) {
        return toTitleCase(string, Pattern.compile("\\s"));
    }

    public static String toTitleCase(final String string, final Pattern separator) {
        if (string == null)
            return null;

        final StringBuilder result = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : string.toCharArray()) {
            if (separator.matcher(String.valueOf(c)).matches()) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            result.append(c);
        }

        return result.toString();
    }
}
