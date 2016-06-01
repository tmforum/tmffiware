/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014,  Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.upm.fiware.rss.model;

// Generated 10-feb-2012 11:04:29 by Hibernate Tools 3.4.0.CR1

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * BmPbMop generated by hbm2java.
 */
@Entity
@Table(name = "bm_pb_mop")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class BmPbMop implements java.io.Serializable {

    private BmPbMopId id;
    private BmMethodsOfPayment bmMethodsOfPayment;
    private BmPaymentbroker bmPaymentbroker;
    private String txPaymentmethodCode;

    /**
     * Constructor.
     */
    public BmPbMop() {
    }

    /**
     * Constructor.
     * 
     * @param id
     * @param bmMethodsOfPayment
     * @param bmPaymentbroker
     */
    public BmPbMop(BmPbMopId id, BmMethodsOfPayment bmMethodsOfPayment, BmPaymentbroker bmPaymentbroker) {
        this.id = id;
        this.bmMethodsOfPayment = bmMethodsOfPayment;
        this.bmPaymentbroker = bmPaymentbroker;
    }

    /**
     * Constructor.
     * 
     * @param id
     * @param bmMethodsOfPayment
     * @param bmPaymentbroker
     * @param txPaymentmethodCode
     */
    public BmPbMop(BmPbMopId id, BmMethodsOfPayment bmMethodsOfPayment, BmPaymentbroker bmPaymentbroker,
        String txPaymentmethodCode) {
        this.id = id;
        this.bmMethodsOfPayment = bmMethodsOfPayment;
        this.bmPaymentbroker = bmPaymentbroker;
        this.txPaymentmethodCode = txPaymentmethodCode;
    }

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "bmPbId", column = @Column(name = "BM_PB_ID", nullable = false, precision = 10,
            scale = 0)),
        @AttributeOverride(name = "nuMopId", column = @Column(name = "NU_MOP_ID", nullable = false, precision = 10,
            scale = 0))
    })
    public BmPbMopId getId() {
        return this.id;
    }

    public void setId(BmPbMopId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NU_MOP_ID", nullable = false, insertable = false, updatable = false)
    public BmMethodsOfPayment getBmMethodsOfPayment() {
        return this.bmMethodsOfPayment;
    }

    public void setBmMethodsOfPayment(BmMethodsOfPayment bmMethodsOfPayment) {
        this.bmMethodsOfPayment = bmMethodsOfPayment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BM_PB_ID", nullable = false, insertable = false, updatable = false)
    public BmPaymentbroker getBmPaymentbroker() {
        return this.bmPaymentbroker;
    }

    public void setBmPaymentbroker(BmPaymentbroker bmPaymentbroker) {
        this.bmPaymentbroker = bmPaymentbroker;
    }

    @Column(name = "TX_PAYMENTMETHOD_CODE", length = 20)
    public String getTxPaymentmethodCode() {
        return this.txPaymentmethodCode;
    }

    public void setTxPaymentmethodCode(String txPaymentmethodCode) {
        this.txPaymentmethodCode = txPaymentmethodCode;
    }

}
