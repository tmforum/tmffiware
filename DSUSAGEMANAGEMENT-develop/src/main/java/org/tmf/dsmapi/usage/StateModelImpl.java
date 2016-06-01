package org.tmf.dsmapi.usage;

import org.tmf.dsmapi.usage.model.Status;
import org.tmf.dsmapi.commons.workflow.StateModelBase;

/**
 *
 * @author maig7313
 */
public class StateModelImpl extends StateModelBase<Status> {
    
    /**
     *
     */
    public StateModelImpl() {
        super(Status.class);
    }    

    /**
     *
     */
    @Override
    protected void draw() {
        // First
        from(Status.Received).to(
                Status.Rejected,
                Status.Guided);

        // Somewhere
        from(Status.Rejected).to(
                Status.Recycled);       
        from(Status.Guided).to(
                Status.Rated,
                Status.Rejected);
        from(Status.Recycled).to(
                Status.Rejected,
                Status.Guided);
        from(Status.Rated).to(
                Status.Rerate);
        from(Status.Rerate).to(
                Status.Rated);

        // Final
        from(Status.Billed);
//        from(Status.REJECTED);
    }
}
