package org.tmf.dsmapi.usageSpecification.event;

public enum UsageSpecificationEventTypeEnum {

    UsageSpecificationCreateNotification("UsageSpecificationCreateNotification"),
    UsageSpecificationUpdateNotification("UsageSpecificationUpdateNotification"),
    UsageSpecificationDeleteNotification("UsageSpecificationDeleteNotification");

    private String text;

    UsageSpecificationEventTypeEnum(String text) {
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
    public static UsageSpecificationEventTypeEnum fromString(String text) {
        if (text != null) {
            for (UsageSpecificationEventTypeEnum b : UsageSpecificationEventTypeEnum.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }
}