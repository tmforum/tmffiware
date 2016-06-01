package org.tmf.dsmapi.commons.utils;

import org.tmf.dsmapi.product.model.State;


public class WorkflowValidator {

    public static boolean isCorrect(State current, State next) {
        

        boolean valid = false;

        switch (current) {
            case Created:
                switch (next) {
                    case Pending_active:
                    case Active:
                        valid = true;
                        break;
                }
                break;
            case Active:
                switch (next) {
                    case Suspended:
                    case Pending_terminate:
                    case Terminated:
                        valid = true;
                        break;
                }
                break;
            case Pending_active:
                switch (next) {
                    case Aborted:
                    case Active:
                    case Cancelled:
                        valid = true;
                        break;
                }
                break;
            case Pending_terminate:
                switch (next) {
                    case Terminated:
                        valid = true;
                        break;
                }
                break;
        }
        return valid;
    }

}
