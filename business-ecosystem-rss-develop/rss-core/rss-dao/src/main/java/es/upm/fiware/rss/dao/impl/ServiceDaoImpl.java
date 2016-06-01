/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 *
 * Copyright (C) 2015 CoNWeT Lab., Universidad Politécnica de Madrid
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

package es.upm.fiware.rss.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import es.upm.fiware.rss.dao.ServiceDao;
import es.upm.fiware.rss.model.BmService;

/**
 * 
 * 
 */
@Repository
public class ServiceDaoImpl extends GenericDaoImpl<BmService, Long> implements ServiceDao {

    /*
     * (non-Javadoc)
     * 
     * @see es.upm.greta.bmms.dao.impl.GenericDaoImpl#getDomainClass()
     */
    @Override
    protected Class<BmService> getDomainClass() {
        return BmService.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.upm.greta.bmms.dao.ServiceDao#getServiceByName(java.lang.String)
     */
    @Override
    public BmService getServiceByName(final String name) {
        Criteria criteria = getSession().createCriteria(BmService.class);
        criteria.add(Restrictions.eq("txName", name));
        List<BmService> result = criteria.list();
        if (result != null && result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

}
