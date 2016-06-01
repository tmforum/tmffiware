/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 *
 * Copyright (C) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
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

package es.upm.fiware.rss.ws;

import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import es.upm.fiware.rss.model.CDR;
import es.upm.fiware.rss.model.RSUser;
import es.upm.fiware.rss.service.CdrsManager;
import es.upm.fiware.rss.service.UserManager;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;


public class CdrsServiceTest {

    @Mock private UserManager userManager;
    @Mock private CdrsManager cdrsManager;
    @InjectMocks private CdrsService toTest;

    private RSUser user;
    private final String aggregatorId = "aggregator@mail.com";
    private final String providerId = "providerId";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.user = new RSUser();
        this.user.setId("userId");
        this.user.setDisplayName("username");
        this.user.setEmail("user@mail.com");

        when(userManager.getCurrentUser()).thenReturn(this.user);
    }

    @Test
    public void createCdr() throws Exception {
        List <CDR> list = new LinkedList<>();
        when(userManager.isAdmin()).thenReturn(true);

        toTest.createCdr(list);
        verify(cdrsManager).createCDRs(list);
    }

    @Test
    public void createCdrNotAllowed() throws Exception {
        List<CDR> list = new LinkedList<>();

        try {
            toTest.createCdr(list);
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            Assert.assertEquals("Operation is not allowed: You are not allowed to create transactions", e.getMessage());
        }
    }

    @Test
    public void getProviderCDRs() throws Exception {
        List <CDR> list = new LinkedList<>();
        when(cdrsManager.getCDRs(this.aggregatorId, this.providerId)).thenReturn(list);

        Map<String, String> ids = new HashMap<>();
        ids.put("provider", this.providerId);
        ids.put("aggregator", this.aggregatorId);

        when(this.userManager.getAllowedIds(
                this.aggregatorId, this.providerId, "transactions")).thenReturn(ids);

        Response response = toTest.getCDRs(this.aggregatorId, this.providerId);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(list, response.getEntity());
    }
}
