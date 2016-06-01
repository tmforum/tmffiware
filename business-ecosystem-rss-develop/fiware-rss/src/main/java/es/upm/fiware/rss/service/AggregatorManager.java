/**
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
package es.upm.fiware.rss.service;

import es.upm.fiware.rss.dao.DbeAggregatorDao;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.model.DbeAggregator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author fdelavega
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AggregatorManager {

    /***
     * Logging system.
     */
    private final Logger logger = LoggerFactory.getLogger(AggregatorManager.class);

    /**
     * 
     */
    @Autowired
    private DbeAggregatorDao aggregatorDao;

    private boolean isValidEmail(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private List<DbeAggregator> getAggregators() {
        return aggregatorDao.getAll();
    }

    /**
     * Creates a new aggregator.
     * 
     * @param aggregator, Aggregator instance
     * @throws RSSException, If the aggregator info is not valid
     */
    public void createAggretator(Aggregator aggregator) throws RSSException {
        logger.debug("Creating aggregator: {}", aggregator.getAggregatorId());

        // Validate aggregator fields
        if (aggregator.getAggregatorId() == null || aggregator.getAggregatorId().isEmpty()) {
            String[] args = {"AggregatorID field is required for creating an aggregator"};
            throw new RSSException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, args);
        }

        if (!this.isValidEmail(aggregator.getAggregatorId())) {
            String[] args = {"AggregatorID field must be an email identifiying a valid Store owner"};
            throw new RSSException(UNICAExceptionType.INVALID_PARAMETER, args);
        }

        if (aggregator.getAggregatorName() == null || aggregator.getAggregatorName().isEmpty()) {
            String[] args = {"AggregatorName field is required for creating an aggregator"};
            throw new RSSException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, args);
        }

        // Check that the aggregator does not exists
        boolean aggExists = true;
        try {
            this.getAggregator(aggregator.getAggregatorId());
        } catch (RSSException e) {
            aggExists = false;
        }

        if (aggExists) {
            String[] args = {"The Aggregator " + aggregator.getAggregatorId() + " already exists"};
            throw  new RSSException(UNICAExceptionType.RESOURCE_ALREADY_EXISTS, args);
        }

        // If this is the first aggegator it must be the default one
        if (this.aggregatorDao.getAll().isEmpty()) {
            aggregator.setDefaultAggregator(true);
        }

        // Change the default aggregator if needed
        if (aggregator.isDefaultAggregator()) {
            DbeAggregator agg = this.aggregatorDao.getDefaultAggregator();

            if (agg != null) {
                agg.setDefaultAggregator(false);
            }
        }

        // Build new aggregator object
        DbeAggregator dbAggregator = new DbeAggregator();
        dbAggregator.setTxEmail(aggregator.getAggregatorId());
        dbAggregator.setTxName(aggregator.getAggregatorName());
        dbAggregator.setDefaultAggregator(aggregator.isDefaultAggregator());

        // Save aggregator to the DB
        this.aggregatorDao.create(dbAggregator);
    }

    public Aggregator buildAPIAggregator(DbeAggregator ag) {
        Aggregator aggregator = new Aggregator();
        aggregator.setAggregatorId(ag.getTxEmail());
        aggregator.setAggregatorName(ag.getTxName());
        aggregator.setDefaultAggregator(ag.isDefaultAggregator());

        return aggregator;
    }

    /**
     * Get existing aggregators from the DB in a format ready to be serialized
     * @return, A list of Aggregator instances with the information of the
     * existing aggregators
     */
    public List<Aggregator> getAPIAggregators() {
            
        List<Aggregator> apiAggregators = new ArrayList<>();
        List<DbeAggregator> aggregators = this.getAggregators();

        aggregators.stream().map((aggregator) -> {
            return this.buildAPIAggregator(aggregator);
        }).forEach((apiAggregator) -> {
            apiAggregators.add(apiAggregator);
        });

        return apiAggregators;
    }

    /**
     * Retrieves an aggregator object using its id
     * @param aggregatorId, identifier of the aggregator to be retrieved
     * @return An Aggregator instance with the information of the specified
     * aggregator
     * @throws RSSException, if the specified aggrgator does not exists
     */
    public Aggregator getAggregator(String aggregatorId) throws RSSException {
        DbeAggregator ag = aggregatorDao.getById(aggregatorId);

        if (ag == null) {
            String[] args = {aggregatorId};
            throw new RSSException(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, args);
        }
        return this.buildAPIAggregator(ag);
    }

    public Aggregator getDefaultAggregator () throws RSSException {
        DbeAggregator aggregator = this.aggregatorDao.getDefaultAggregator();

        if (aggregator == null) {
            String[] args = {"There is not any aggregator registered"};
            throw new RSSException(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, args);
        }

        return this.buildAPIAggregator(aggregator);
    }
}
