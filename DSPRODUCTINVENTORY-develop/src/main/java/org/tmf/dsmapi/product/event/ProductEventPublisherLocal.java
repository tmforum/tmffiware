package org.tmf.dsmapi.product.event;

import java.util.Date;
import javax.ejb.Local;
import org.tmf.dsmapi.product.model.Product;
import org.tmf.dsmapi.product.event.ProductEvent;


@Local
public interface ProductEventPublisherLocal {

    void publish(ProductEvent event);

    /**
     *
     * CreateNotification
     * @param bean the bean which has been created
     * @param reason the related reason
     * @param date the creation date
     */
    public void createNotification(Product bean, Date date);

    /**
     *
     * DeletionNotification
     * @param bean the bean which has been deleted
     * @param reason the reason of the deletion
     * @param date the deletion date
     */
    public void deletionNotification(Product bean, Date date);

    /**
     *
     * UpdateNotification (PATCH)
     * @param bean the bean which has been updated
     * @param reason the reason it has been updated for
     * @param date the update date
     */
    public void updateNotification(Product bean, Date date);

    /**
     *
     * ValueChangeNotification
     * @param bean the bean which has been changed
     * @param reason the reason it was changed
     * @param date the change date
     */
    public void valueChangedNotification(Product bean, Date date);

    /**
     *
     * StatusChangedNotification
     * @param bean the bean which has been updated
     * @param reason the reason it has been updated for
     * @param date the update date
     */
    public void statusChangedNotification(Product bean, Date date);

}
