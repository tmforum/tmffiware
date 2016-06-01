/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 *
 * Copyright (C) 2015, CoNWeT Lab., Universidad Politénica de Madrid
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

/**
 * 
 */
package es.upm.fiware.rss.dao.impl.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.upm.fiware.rss.common.test.DatabaseLoader;
import es.upm.fiware.rss.dao.MethodPaymentDao;
import es.upm.fiware.rss.model.BmMethodsOfPayment;

/**
 * 
 */
@ContextConfiguration({"classpath:database.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class MethodPaymentDaoImplTest {

    /**
     * DAO for country.
     */
    @Autowired
    private MethodPaymentDao methodPaymentDao;

    @Autowired
    private DatabaseLoader databaseLoader;
    
    /*@Autowired
    @Qualifier("transactionManager")
    private HibernateTransactionManager transactionManager;*/

    /**
     * Method to insert data before test.
     * 
     * @throws Exception
     *             from db
     */
    @Before
    public void setUp() throws Exception {
        databaseLoader.cleanInsert("dbunit/CREATE_DATATEST_TRANSACTIONS.xml", true);
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        databaseLoader.deleteAll("dbunit/CREATE_DATATEST_TRANSACTIONS.xml", true);
    }

    /**
     * Test method for {@link es.upm.fiware.rss.dao.impl.MethodPaymentDaoImpl#getMopByName(java.lang.String)}.
     */
    public void testGetMopByName() {
        // Call method to test
        BmMethodsOfPayment c = methodPaymentDao.getMopByName("Visa");

        // Check result
        Assert.assertNotNull("BmMethodsOfPayment is null", c);
        Assert.assertTrue("ID not equal", c.getNuMopId() == 1);
        Assert.assertTrue("Name not equal", c.getTxName().equalsIgnoreCase("Visa"));
        Assert.assertTrue("Description not equal", c.getTxDescription().equalsIgnoreCase("Pago con tarjeta"));
        Assert.assertTrue("Name not equal", c.getTxCode().equalsIgnoreCase("CreditCard"));

    }

    /**
     * Test method for {@link es.upm.fiware.rss.dao.impl.MethodPaymentDaoImpl#getMopByCode(java.lang.String)}.
     */
    public void testGetMopByCode() {
        // Call method to test
        BmMethodsOfPayment c = methodPaymentDao.getMopByCode("CreditCard");

        // Check result
        Assert.assertNotNull("BmMethodsOfPayment is null", c);
        Assert.assertTrue("ID not equal", c.getNuMopId() == 1);
        Assert.assertTrue("Name not equal", c.getTxName().equalsIgnoreCase("Visa"));
        Assert.assertTrue("Description not equal", c.getTxDescription().equalsIgnoreCase("Pago con tarjeta"));
        Assert.assertTrue("Name not equal", c.getTxCode().equalsIgnoreCase("CreditCard"));

    }
}
