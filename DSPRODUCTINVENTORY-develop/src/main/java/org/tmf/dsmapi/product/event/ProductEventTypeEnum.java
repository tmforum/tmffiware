/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tmf.dsmapi.product.event;

public enum ProductEventTypeEnum {

    ProductCreationNotification("ProductCreationNotification"),
    ProductUpdateNotification("ProductUpdateNotification"),
    ProductDeletionNotification("ProductDeletionNotification"),
    ProductValueChangeNotification("ProductValueChangeNotification"),
    ProductStatusChangeNotification("ProductStatusChangeNotification");

    private String text;

    ProductEventTypeEnum(String text) {
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
    public static org.tmf.dsmapi.product.event.ProductEventTypeEnum fromString(String text) {
        if (text != null) {
            for (ProductEventTypeEnum b : ProductEventTypeEnum.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }
}