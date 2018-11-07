package com.meituan.service.mobile.mtthrift.util;

public class VersionComparator {

    public static boolean equals(Object o1, Object o2) {
        return compare(o1, o2) == 0;
    }

    public static int compare(Object o1, Object o2) {
        String version1 = (String) o1;
        String version2 = (String) o2;

        VersionTokenizer tokenizer1 = new VersionTokenizer(version1);
        VersionTokenizer tokenizer2 = new VersionTokenizer(version2);

        int number1 = 0, number2 = 0;
        String suffix1 = "", suffix2 = "";

        while (tokenizer1.MoveNext()) {
            if (!tokenizer2.MoveNext()) {
                do {
                    number1 = tokenizer1.getNumber();
                    suffix1 = tokenizer1.getSuffix();
                    if (number1 != 0 || suffix1.length() != 0) {
                        // Version one is longer than number two, and non-zero
                        return 1;
                    }
                }
                while (tokenizer1.MoveNext());

                // Version one is longer than version two, but zero
                return 0;
            }

            number1 = tokenizer1.getNumber();
            suffix1 = tokenizer1.getSuffix();
            number2 = tokenizer2.getNumber();
            suffix2 = tokenizer2.getSuffix();

            if (number1 < number2) {
                // Number one is less than number two
                return -1;
            }
            if (number1 > number2) {
                // Number one is greater than number two
                return 1;
            }

            boolean empty1 = suffix1.length() == 0;
            boolean empty2 = suffix2.length() == 0;

            if (empty1 && empty2) continue; // No suffixes
            if (empty1) return 1; // First suffix is empty (1.2 > 1.2b)
            if (empty2) return -1; // Second suffix is empty (1.2a < 1.2)

            // Lexical comparison of suffixes
            int result = suffix1.compareTo(suffix2);
            if (result != 0) return result;

        }
        if (tokenizer2.MoveNext()) {
            do {
                number2 = tokenizer2.getNumber();
                suffix2 = tokenizer2.getSuffix();
                if (number2 != 0 || suffix2.length() != 0) {
                    // Version one is longer than version two, and non-zero
                    return -1;
                }
            }
            while (tokenizer2.MoveNext());

            // Version two is longer than version one, but zero
            return 0;
        }
        return 0;
    }

    public static void main (String[] args)
    {
        test(new String[]{"1.1.2", "1.2", "1.2.0", "1.2.1", "1.12"});
        test(new String[]{"1.3", "1.3a", "1.3b", "1.3-SNAPSHOT"});
    }

    private static void test(String[] versions) {
        for (int i = 0; i < versions.length; i++) {
            for (int j = i; j < versions.length; j++) {
                test(versions[i], versions[j]);
            }
        }
    }

    private static void test(String v1, String v2) {
        int result = VersionComparator.compare(v1, v2);
        String op = "==";
        if (result < 0) op = "<";
        if (result > 0) op = ">";
        System.out.printf("%s %s %s\n", v1, op, v2);
    }
}

class VersionTokenizer {
    private final String versionString;
    private final int length;

    private int position;
    private int number;
    private String suffix;

    int getNumber() {
        return number;
    }

    String getSuffix() {
        return suffix;
    }

    VersionTokenizer(String versionString) {
        if (versionString == null)
            throw new IllegalArgumentException("versionString is null");

        this.versionString = versionString;
        length = versionString.length();
    }

    boolean MoveNext() {
        number = 0;
        suffix = "";

        // No more characters
        if (position >= length)
            return false;

        while (position < length) {
            char c = versionString.charAt(position);
            if (c < '0' || c > '9') break;
            number = number * 10 + (c - '0');
            position++;
        }

        int suffixStart = position;

        while (position < length) {
            char c = versionString.charAt(position);
            if (c == '.') break;
            position++;
        }

        suffix = versionString.substring(suffixStart, position);

        if (position < length) position++;

        return true;
    }
}