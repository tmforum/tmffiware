package org.tmf.dsmapi.catalog.resource;

import java.util.EnumSet;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.tmf.dsmapi.commons.exceptions.InvalidEnumeratedValueException;

/**
 *
 * @author bahman.barzideh
 *
 */
public enum LifecycleStatus {
    IN_STUDY  ("In Study"),
    IN_DESIGN ("In Design"),
    IN_TEST   ("In Test"),
    ACTIVE    ("Active"),
    LAUNCHED  ("Launched"),
    RETIRED   ("Retired"),
    OBSOLETE  ("Obsolete"),
    REJECTED  ("Rejected");

    private String value;

    private LifecycleStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonValue(true)
    public String getValue() {
        return this.value;
    }

    public boolean canTransitionFrom(LifecycleStatus currentStatus) {
        if (this == currentStatus) {
            return true;
        }

        switch(this) {
            case IN_STUDY: {
                if (currentStatus == null) {
                    return true;
                }

                return false;
            }

            case IN_DESIGN: {
                if (currentStatus == null || currentStatus == IN_STUDY || currentStatus == IN_TEST) {
                    return true;
                }

                return false;
            }

            case IN_TEST : {
                if (currentStatus == null || currentStatus == IN_DESIGN) {
                    return true;
                }

                return false;
            }

            case ACTIVE : {
                if (currentStatus == null || currentStatus == IN_TEST) {
                    return true;
                }

                return false;
            }

            case LAUNCHED : {
                if (currentStatus == null || currentStatus == ACTIVE) {
                    return true;
                }

                return false;
            }

            case RETIRED : {
                if (currentStatus == ACTIVE || currentStatus == LAUNCHED) {
                    return true;
                }

                return false;
            }

            case OBSOLETE : {
                if (currentStatus == RETIRED) {
                    return true;
                }

                return false;
            }

            case REJECTED : {
                if (currentStatus == IN_TEST) {
                    return true;
                }

                return false;
            }

            default : {
                return false;
            }
        }
    }

    public static Set<LifecycleStatus> transitionableStatues(LifecycleStatus lifecycleStatus) {

        if (lifecycleStatus == null) {
            return EnumSet.of(LifecycleStatus.IN_STUDY, LifecycleStatus.IN_DESIGN, LifecycleStatus.IN_TEST, LifecycleStatus.ACTIVE, LifecycleStatus.LAUNCHED);
        }

        switch (lifecycleStatus) {
            case IN_STUDY: {
                return EnumSet.of(LifecycleStatus.IN_DESIGN);
            }

            case IN_DESIGN: {
                return EnumSet.of(LifecycleStatus.IN_TEST);
            }

            case IN_TEST : {
                return EnumSet.of(LifecycleStatus.IN_DESIGN, LifecycleStatus.ACTIVE, LifecycleStatus.REJECTED);
            }

            case ACTIVE : {
                return EnumSet.of(LifecycleStatus.LAUNCHED, LifecycleStatus.RETIRED);
            }

            case LAUNCHED : {
                return EnumSet.of(LifecycleStatus.RETIRED);
            }

            case RETIRED : {
                return EnumSet.of(LifecycleStatus.OBSOLETE);
            }

            case OBSOLETE : {
                return null;
            }

            case REJECTED : {
                return null;
            }

            default : {
                return EnumSet.noneOf(LifecycleStatus.class);
            }
        }
    }

    public static LifecycleStatus find(String value) {
        for (LifecycleStatus lifecycleStatus : values()) {
            if (lifecycleStatus.value.equals(value)) {
                return lifecycleStatus;
            }
        }

        return null;
    }

    @JsonCreator
    public static LifecycleStatus fromJson(String value) throws InvalidEnumeratedValueException {
        if (value == null) {
            return null;
        }

        LifecycleStatus enumeratedValue = LifecycleStatus.find(value);
        if (enumeratedValue != null) {
            return enumeratedValue;
        }

        throw new InvalidEnumeratedValueException(value, EnumSet.allOf(LifecycleStatus.class));
    }
}
