package org.tmf.dsmapi.usageSpecification.event;

import java.util.Date;
import javax.ejb.Local;
import org.tmf.dsmapi.usage.model.UsageSpecification;


@Local
public interface UsageSpecificationEventPublisherLocal {

    void publish(UsageSpecificationEvent event);

    /**
     *
     * CreateNotification
     * @param bean the bean which has been created
     * @param reason the related reason
     * @param date the creation date
     */
    public void createNotification(UsageSpecification bean, Date date);

    /**
     *
     * DeletionNotification
     * @param bean the bean which has been deleted
     * @param reason the reason of the deletion
     * @param date the deletion date
     */
    public void deletionNotification(UsageSpecification bean, Date date);

    /**
     *
     * UpdateNotification (PATCH)
     * @param bean the bean which has been updated
     * @param reason the reason it has been updated for
     * @param date the update date
     */
    public void updateNotification(UsageSpecification bean, Date date);

}
