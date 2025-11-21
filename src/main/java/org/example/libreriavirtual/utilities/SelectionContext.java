package org.example.libreriavirtual.utilities;

public final class SelectionContext {
    private static volatile String gradeName;
    private static volatile String sectionName;

    private SelectionContext() { /* util */ }

    public static void setGradeName(String name) {
        gradeName = name;
    }

    public static void setSectionName(String name) {
        sectionName = name;
    }

    public static String getGradeName() {
        return gradeName;
    }

    public static String getSectionName() {
        return sectionName;
    }

    public static void clear() {
        gradeName = null;
        sectionName = null;
    }
}
