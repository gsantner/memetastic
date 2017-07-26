/*
 * ---------------------------------------------------------------------------- *
 * Gregor Santner <gsantner.github.io> wrote this file. You can do whatever
 * you want with this stuff. If we meet some day, and you think this stuff is
 * worth it, you can buy me a coke in return. Provided as is without any kind
 * of warranty. No attribution required.                  - Gregor Santner
 *
 * License: Creative Commons Zero (CC0 1.0)
 *  http://creativecommons.org/publicdomain/zero/1.0/
 * ----------------------------------------------------------------------------
 */
 
 /*
 * Get updates:
 *  https://github.com/gsantner/onePieceOfCode/blob/master/java/SimpleMarkdownParser.java
 * Apply to TextView:
 *   See https://github.com/gsantner/onePieceOfCode/blob/master/android/Helpers.get().java
 * Parses most common markdown tags. Only inline tags are supported, multiline/block syntax
 * is not supported (citation, multiline code, ..). This is intended to stay as easy as possible.
 *
 * You can e.g. apply a accent color by replacing #000001 with your accentColor string.
 *
 * FILTER_ANDROID_TEXTVIEW output is intended to be used at simple Android TextViews,
 * were a limited set of html tags is supported. This allow to still display e.g. a simple
 * CHANGELOG.md file without inlcuding a WebView for showing HTML, or other additional UI-libraries.
 *
 * FILTER_HTMLPART is intended to be used at engines understanding most common HTML tags.
 */

package io.github.gsantner.opoc.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple Markdown Parser
 */
@SuppressWarnings({"WeakerAccess", "CaughtExceptionImmediatelyRethrown"})
public class SimpleMarkdownParser {

    public interface SimpleLineFilter {
        String filterLine(String line);
    }

    public static final SimpleLineFilter FILTER_ANDROID_TEXTVIEW = new SimpleLineFilter() {
        @Override
        public String filterLine(String line) {
            // TextView supports a limited set of html tags, most notably
            // a href, b, big, font size&color, i, li, small, u
            line = line
                    .replace("~°", "&nbsp;&nbsp;") // double space/half tab
                    .replaceAll("^### ([^<]*)", "<br/><big><b><font color='#000000'>$1</font></b></big>  ") // h3
                    .replaceAll("^## ([^<]*)", "<br/><big><big><b><font color='#000000'>$1</font></b></big></big><br/>  ") // h2 (DEP: h3)
                    .replaceAll("^# ([^<]*)", "<br/><big><big><big><b><font color='#000000'>$1</font></b></big></big></big><br/>  ") // h1 (DEP: h2,h3)
                    .replaceAll("!\\[(.*?)\\]\\((.*?)\\)", "<a href=\\'$2\\'>$1</a>") // img
                    .replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\\'$2\\'>$1</a>") // a href (DEP: img)
                    .replaceAll("<(http|https):\\/\\/(.*)>", "<a href='$1://$2'>$1://$2</a>") // a href (DEP: img)
                    .replaceAll("^(-|\\*) ([^<]*)", "<font color='#000001'>&#8226;</font> $2  ") // unordered list + end line
                    .replaceAll("^  (-|\\*) ([^<]*)", "&nbsp;&nbsp;<font color='#000001'>&#8226;</font> $2  ") // unordered list2 + end line
                    .replaceAll("`([^<]*)`", "<font face='monospace'>$1</font>") // code
                    .replace("\\*", "●") // temporary replace escaped star symbol
                    .replaceAll("\\*\\*([^<]*)\\*\\*", "<b>$1</b>") // bold (DEP: temp star)
                    .replaceAll("\\*([^<]*)\\*", "<i>$1</i>") // italic (DEP: temp star code)
                    .replace("●", "*") // restore escaped star symbol (DEP: b,i)
                    .replaceAll("  $", "<br/>") // new line (DEP: ul)
            ;
            return line.isEmpty() ? line + "<br/>" : line;
        }
    };

    private static SimpleMarkdownParser instance;

    public static SimpleMarkdownParser get() {
        if (instance == null) {
            instance = new SimpleMarkdownParser();
        }
        return instance;
    }

    public static final SimpleLineFilter FILTER_HTMLPART = new SimpleLineFilter() {
        @Override
        public String filterLine(String line) {
            line = line
                    .replaceAll("~°", "&nbsp;&nbsp;") // double space/half tab
                    .replaceAll("^### ([^<]*)", "<h3>$1</h3>") // h3
                    .replaceAll("^## ([^<]*)", "<h2>$1</h2>") /// h2 (DEP: h3)
                    .replaceAll("^# ([^<]*)", "<h1>$1</h1>") // h1 (DEP: h2,h3)
                    .replaceAll("!\\[(.*?)\\]\\((.*?)\\)", "<img src=\\'$2\\' alt='$1' />") // img
                    .replaceAll("<(http|https):\\/\\/(.*)>", "<a href='$1://$2'>$1://$2</a>") // a href (DEP: img)
                    .replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\\'$2\\'>$1</a>") // a href (DEP: img)
                    .replaceAll("^(-|\\*) ([^<]*)", "<font color='#000001'>&#8226;</font> $2  ") // unordered list + end line
                    .replaceAll("^  (-|\\*) ([^<]*)", "&nbsp;&nbsp;<font color='#000001'>&#8226;</font> $2  ") // unordered list2 + end line
                    .replaceAll("`([^<]*)`", "<code>$1</code>") // code
                    .replace("\\*", "●") // temporary replace escaped star symbol
                    .replaceAll("\\*\\*([^<]*)\\*\\*", "<b>$1</b>") // bold (DEP: temp star)
                    .replaceAll("\\*([^<]*)\\*", "<b>$1</b>") // italic (DEP: temp star)
                    .replace("●", "*") // restore escaped star symbol (DEP: b,i)
                    .replaceAll("  $", "<br/>") // new line (DEP: ul)
            ;
            return line.isEmpty() ? line + "<br/>" : line;
        }
    };

    //########################
    //##     Members
    //########################
    private String html;

    public SimpleMarkdownParser parse(String filepath, SimpleLineFilter simpleLineFilter) throws IOException {
        return parse(new FileInputStream(filepath), simpleLineFilter, "");
    }

    public SimpleMarkdownParser parse(InputStream inputStream, SimpleLineFilter simpleLineFilter, String lineMdPrefix) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        String line;

        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                sb.append(simpleLineFilter.filterLine(lineMdPrefix + line));
                sb.append("\n");
            }
        } catch (IOException rethrow) {
            html = "";
            throw rethrow;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        html = sb.toString().trim();
        return this;
    }

    public SimpleMarkdownParser parse(String markdown, SimpleLineFilter simpleLineFilter, String lineMdPrefix) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (String line : markdown.split("\n")) {
            sb.append(simpleLineFilter.filterLine(lineMdPrefix + line));
            sb.append("\n");
        }

        html = sb.toString().trim();
        return this;
    }

    public String getHtml() {
        return html;
    }

    public SimpleMarkdownParser setHtml(String html) {
        this.html = html;
        return this;
    }

    public SimpleMarkdownParser removeMultiNewlines() {
        html = html.replace("\n", "").replaceAll("(<br/>){3,}", "<br/><br/>");
        return this;
    }

    public SimpleMarkdownParser replaceBulletCharacter(String replacment) {
        html = html.replace("&#8226;", replacment);
        return this;
    }

    public SimpleMarkdownParser replaceColor(String hexColor, int newIntColor) {
        html = html.replace(hexColor, String.format("#%06X", 0xFFFFFF & newIntColor));
        return this;
    }

    @Override
    public String toString() {
        return html != null ? html : "";
    }
}
