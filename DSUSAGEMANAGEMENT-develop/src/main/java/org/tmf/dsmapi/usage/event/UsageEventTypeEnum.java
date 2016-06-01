package org.tmf.dsmapi.usage.event;

public enum UsageEventTypeEnum {

    UsageCreateNotification("UsageCreateNotification"),
    UsageUpdateNotification("UsageUpdateNotification"),
    UsageDeleteNotification("UsageDeleteNotification"),
    UsageStatusChangedNotification("UsageStatusChangedNotification");
    

    private String text;

    UsageEventTypeEnum(String text) {
        this.text = text;
    }

    /**
     *
     * @return
     */
    public String getText() {
        return this.text;
    }

    /**
     *
     * @param text
     * @return
     */
    public static UsageEventTypeEnum fromString(String text) {
        if (text != null) {
            for (UsageEventTypeEnum b : UsageEventTypeEnum.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }
}