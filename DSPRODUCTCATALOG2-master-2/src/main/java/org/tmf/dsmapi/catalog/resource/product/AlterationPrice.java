package org.tmf.dsmapi.catalog.resource.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.tmf.dsmapi.commons.OutputUtilities;
import org.tmf.dsmapi.commons.Utilities;

/**
 *
 * @author bahman.barzideh
 * @author fdelavega
 *
 * {
 *     "taxIncludedAmount": "12.00",
 *     "dutyFreeAmount": "10.00",
 *     "taxRate": "20.00",
 *     "percentage": 0
 * }
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Embeddable
public class AlterationPrice implements Serializable {
    private final static long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(ProductOffering.class.getName());

    @Column(name = "PRICE_ALT_TAX_INCLUDED_AMOUNT", nullable = true)
    BigDecimal taxIncludedAmount;

    @Column(name = "PRICE_ALT_DUTY_FREE_AMOUNT", nullable = true)
    BigDecimal dutyFreeAmount;

    @Column(name = "PRICE_ALT_TAX_RATE", nullable = true)
    BigDecimal taxRate;

    @Column(name = "PRICE_ALT_PERCENTAGE", nullable = true)
    String percentage;

    public AlterationPrice() {
    }

    public BigDecimal getTaxIncludedAmount() {
        return taxIncludedAmount;
    }

    public void setTaxIncludedAmount(BigDecimal taxIncludedAmount) {
        this.taxIncludedAmount = taxIncludedAmount;
    }

    public BigDecimal getDutyFreeAmount() {
        return dutyFreeAmount;
    }

    public void setDutyFreeAmount(BigDecimal dutyFreeAmount) {
        this.dutyFreeAmount = dutyFreeAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    @JsonProperty(value = "taxIncludedAmount")
    public String taxIncludedAmountToJson() {
        return OutputUtilities.formatCurrency(taxIncludedAmount);
    }

    @JsonProperty(value = "dutyFreeAmount")
    public String dutyFreeAmountToJson() {
        return OutputUtilities.formatCurrency(dutyFreeAmount);
    }

    @JsonProperty(value = "taxRate")
    public String taxRateToJson() {
        return OutputUtilities.formatCurrency(taxRate);
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 89 * hash + (this.taxIncludedAmount != null ? this.taxIncludedAmount.hashCode() : 0);
        hash = 89 * hash + (this.dutyFreeAmount != null ? this.dutyFreeAmount.hashCode() : 0);
        hash = 89 * hash + (this.taxRate != null ? this.taxRate.hashCode() : 0);
        hash = 89 * hash + (this.percentage != null ? this.percentage.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final AlterationPrice other = (AlterationPrice) object;

        return Utilities.areEqual(this.taxIncludedAmount, other.taxIncludedAmount) &&
                Utilities.areEqual(this.dutyFreeAmount, other.dutyFreeAmount) &&
                Utilities.areEqual(this.taxRate, other.taxRate) &&
                Utilities.areEqual(this.percentage, other.percentage);
    }

    @Override
    public String toString() {
        return "AlterationPrice{" + "taxIncludedAmount=" + taxIncludedAmount +
                ", dutyFreeAmount=" + dutyFreeAmount + ", taxRate=" + taxRate +
                "percentage=" + percentage + '}';
    }

    public static AlterationPrice createProto() {
        AlterationPrice alterationPrice = new AlterationPrice();

        alterationPrice.taxIncludedAmount = new BigDecimal(13.00);
        alterationPrice.dutyFreeAmount = new BigDecimal(12.20);
        alterationPrice.taxRate = new BigDecimal(14.01);
        alterationPrice.percentage = "0";

        return alterationPrice;
    }

}
