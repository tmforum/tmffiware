package org.tmf.dsmapi.product;

import org.tmf.dsmapi.commons.workflow.StateModelBase;
import org.tmf.dsmapi.product.model.State;

/**
 *
 * @author maig7313
 */
public class StateModelImpl extends StateModelBase<State> {
    
    /**
     *
     */
    public StateModelImpl() {
        super(State.class);
    }    

    /**
     *
     */
    @Override
    protected void draw() {
        // First
        fromFirst(State.Created).to(State.Pending_active,
                State.Active);

        // Somewhere
        from(State.Pending_active).to(State.Aborted,
                State.Cancelled);
        from(State.Active).to(State.Suspended,
                State.Terminated,
                State.Pending_terminate);       
        from(State.Suspended).to(State.Active);
        from(State.Pending_terminate).to(State.Terminated);

        // Final
        from(State.Aborted);
        from(State.Cancelled);
        from(State.Terminated);
    }
}
