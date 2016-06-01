package org.tmf.dsmapi.productOrder;

import org.tmf.dsmapi.productOrder.event.*;
import java.util.Date;
import java.util.ResourceBundle;
import javax.ejb.Local;
import org.tmf.dsmapi.productOrder.model.Product;
import org.tmf.dsmapi.productOrder.model.ProductOrder;


/**
 *
 * @author pierregauthier
 */
@Local
public interface UpdateInventoryLocal {

    void publish(Event event);

    /**
     *
     * CreateNotification
     * @param bean the bean which has been created
     * @param reason the related reason
     * @param date the creation date
     */
    public void createNotification(ProductOrder bean, Date date);

    /**
     *
     * DeletionNotification
     * @param bean the bean which has been deleted
     * @param reason the reason of the deletion
     * @param date the deletion date
     */
    public void removeNotification(ProductOrder bean, Date date);

    /**
     *
     * orderInformationRequiredNotification 
     * @param bean the bean which has been updated
     * @param reason the reason it has been updated for
     * @param date the update date
     */
    public void orderInformationRequiredNotification(ProductOrder bean, Date date);

    /**
     *
     * ValueChangeNotification (PATCH)
     * @param bean the bean which has been changed
     * @param reason the reason it was changed
     * @param date the change date
     */
    public void valueChangeNotification(ProductOrder bean, Date date);

    /**
     *
     * StatusChangeNotification
     * @param bean the bean which has been updated
     * @param reason the reason it has been updated for
     * @param date the update date
     */
    public void stateChangeNotification(ProductOrder bean, Date date);
    

    public void addToInventory(Product currentProduct);

}
