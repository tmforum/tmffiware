/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package es.upm.fiware.rss.model;

// Generated 20-feb-2012 9:51:24 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * BmService generated by hbm2java.
 */
@Entity
@Table(name = "bm_service")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class BmService implements java.io.Serializable {

    private long nuServiceId;
    private String txName;
    private String txDescription;
    private String tcThirdpartiesYn;
    private Date dtStartDate;
    private String tcStatus;
    private String tcApiTaxesYn;
    private Set<BmProduct> bmProducts = new HashSet<BmProduct>(0);
    private Set<BmServiceDeployment> bmServiceDeployments = new HashSet<BmServiceDeployment>(0);
    private Set<BmServiceProductType> bmServiceProductTypes = new HashSet<BmServiceProductType>(0);

    /**
     * Constructor.
     */
    public BmService() {
    }

    /**
     * Constructor.
     * 
     * @param nuServiceId
     * @param txName
     * @param tcThirdpartiesYn
     * @param dtStartDate
     * @param tcStatus
     * @param tcApiTaxesYn
     */
    public BmService(long nuServiceId, String txName, String tcThirdpartiesYn, Date dtStartDate, String tcStatus,
        String tcApiTaxesYn) {
        this.nuServiceId = nuServiceId;
        this.txName = txName;
        this.tcThirdpartiesYn = tcThirdpartiesYn;
        this.dtStartDate = dtStartDate;
        this.tcStatus = tcStatus;
        this.tcApiTaxesYn = tcApiTaxesYn;
    }

    public BmService(long nuServiceId, String txName, String txDescription, String tcThirdpartiesYn, Date dtStartDate,
        String tcStatus, String tcApiTaxesYn, Long nuNeosdpServiceid, Set<BmProduct> bmProducts,
        Set<BmServiceDeployment> bmServiceDeployments,
        Set<BmServiceProductType> bmServiceProductTypes) {
        this.nuServiceId = nuServiceId;
        this.txName = txName;
        this.txDescription = txDescription;
        this.tcThirdpartiesYn = tcThirdpartiesYn;
        this.dtStartDate = dtStartDate;
        this.tcStatus = tcStatus;
        this.tcApiTaxesYn = tcApiTaxesYn;
        this.bmServiceDeployments = bmServiceDeployments;
        this.bmServiceProductTypes = bmServiceProductTypes;
    }

    @Id
    @Column(name = "NU_SERVICE_ID", unique = true, nullable = false, precision = 10, scale = 0)
    public long getNuServiceId() {
        return this.nuServiceId;
    }

    public void setNuServiceId(long nuServiceId) {
        this.nuServiceId = nuServiceId;
    }

    @Column(name = "TX_NAME", nullable = false, length = 20)
    public String getTxName() {
        return this.txName;
    }

    public void setTxName(String txName) {
        this.txName = txName;
    }

    @Column(name = "TX_DESCRIPTION", length = 250)
    public String getTxDescription() {
        return this.txDescription;
    }

    public void setTxDescription(String txDescription) {
        this.txDescription = txDescription;
    }

    @Column(name = "TC_THIRDPARTIES_YN", nullable = false, length = 1)
    public String getTcThirdpartiesYn() {
        return this.tcThirdpartiesYn;
    }

    public void setTcThirdpartiesYn(String tcThirdpartiesYn) {
        this.tcThirdpartiesYn = tcThirdpartiesYn;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_START_DATE", nullable = false, length = 7)
    public Date getDtStartDate() {
        return this.dtStartDate;
    }

    public void setDtStartDate(Date dtStartDate) {
        this.dtStartDate = dtStartDate;
    }

    @Column(name = "TC_STATUS", nullable = false, length = 1)
    public String getTcStatus() {
        return this.tcStatus;
    }

    public void setTcStatus(String tcStatus) {
        this.tcStatus = tcStatus;
    }

    @Column(name = "TC_API_TAXES_YN", nullable = false, length = 1)
    public String getTcApiTaxesYn() {
        return this.tcApiTaxesYn;
    }

    public void setTcApiTaxesYn(String tcApiTaxesYn) {
        this.tcApiTaxesYn = tcApiTaxesYn;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bmService")
    public Set<BmProduct> getBmProducts() {
        return this.bmProducts;
    }

    public void setBmProducts(Set<BmProduct> bmProducts) {
        this.bmProducts = bmProducts;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bmService")
    public Set<BmServiceDeployment> getBmServiceDeployments() {
        return this.bmServiceDeployments;
    }

    public void setBmServiceDeployments(Set<BmServiceDeployment> bmServiceDeployments) {
        this.bmServiceDeployments = bmServiceDeployments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bmService")
    public Set<BmServiceProductType> getBmServiceProductTypes() {
        return this.bmServiceProductTypes;
    }

    public void setBmServiceProductTypes(Set<BmServiceProductType> bmServiceProductTypes) {
        this.bmServiceProductTypes = bmServiceProductTypes;
    }

}
