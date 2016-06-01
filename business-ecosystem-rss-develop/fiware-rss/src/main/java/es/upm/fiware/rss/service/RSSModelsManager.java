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

package es.upm.fiware.rss.service;

import es.upm.fiware.rss.algorithm.AlgorithmFactory;
import es.upm.fiware.rss.algorithm.AlgorithmProcessor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.upm.fiware.rss.dao.DbeAggregatorDao;
import es.upm.fiware.rss.dao.DbeAppProviderDao;
import es.upm.fiware.rss.dao.ModelProviderDao;
import es.upm.fiware.rss.dao.SetRevenueShareConfDao;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.DbeAggregator;
import es.upm.fiware.rss.model.DbeAppProvider;
import es.upm.fiware.rss.model.ModelProvider;
import es.upm.fiware.rss.model.ModelProviderId;
import es.upm.fiware.rss.model.RSSModel;
import es.upm.fiware.rss.model.SetRevenueShareConf;
import es.upm.fiware.rss.model.SetRevenueShareConfId;
import es.upm.fiware.rss.model.StakeholderModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class RSSModelsManager {

    /***
     * Logging system.
     */
    private final Logger logger = LoggerFactory.getLogger(RSSModelsManager.class);

    /**
     * 
     */
    @Autowired
    private DbeAppProviderDao appProviderDao;

    /**
     * 
     */
    @Autowired
    private SetRevenueShareConfDao revenueShareConfDao;

    @Autowired
    private DbeAggregatorDao aggregatorDao;

    @Autowired
    private ModelProviderDao modelProviderDao;

    /**
     * Check whether a given revenue sharing model exists in the database
     * identified by it aggregator, provider owner and product class
     * @param aggregatorId
     * @param providerId
     * @param productClass
     * @return true if the model identified by aggregatorId, providerId and productClass exists
     */
    public boolean existModel(String aggregatorId, String providerId, String productClass) {
        boolean res = true;

        List<SetRevenueShareConf> result = revenueShareConfDao.getRevenueModelsByParameters(aggregatorId,
            providerId, productClass);

        if (null == result || result.isEmpty()) {
            res = false;
        }
        return res;
    }

    /**
     * Retrives a list of revenue sharing models filtered by aggregator, provider
     * and product class
     * 
     * @param aggregatorId, Id of the aggregator
     * @param appProviderId, Id if the provider owener of the revenue sharing models
     * @param productClass, Product class where the models are applied
     * @return
     * @throws RSSException
     */
    public List<RSSModel> getRssModels(String aggregatorId, String appProviderId,
            String productClass) throws RSSException {
        logger.debug("Into getRssModels() method");

        // Validate owner provider
        if (null != appProviderId && !appProviderId.isEmpty()) {
            checkValidAppProvider(aggregatorId, appProviderId);
        }

        List<RSSModel> models = new ArrayList<>();
        List<SetRevenueShareConf> result = revenueShareConfDao.getRevenueModelsByParameters(aggregatorId,
            appProviderId, productClass);

        // convert result to api model.
        if (null != result && !result.isEmpty()) {
            result.stream().forEach((model) -> {
                models.add(convertIntoApiModel(model));
            });
        }
        return models;
    }

    private SetRevenueShareConfId buildRSModelId(RSSModel rssModel) {
        // Create new model id
        SetRevenueShareConfId id = new SetRevenueShareConfId();
        DbeAppProvider provider = this.appProviderDao.
                getProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());

        id.setModelOwner(provider);
        id.setProductClass(rssModel.getProductClass());
        return id;
    }

    private Set<ModelProvider> buildStakeholdersModel(RSSModel rssModel, SetRevenueShareConf model) {
        Set<ModelProvider> stakeholders =  new HashSet<>();

        rssModel.getStakeholders().stream().map((stakeholderModel) -> {
            DbeAppProvider stakeholder = this.appProviderDao.
                    getProvider(rssModel.getAggregatorId(), stakeholderModel.getStakeholderId());
            // Build stakeholder id
            ModelProviderId stModelId = new ModelProviderId();
            stModelId.setStakeholder(stakeholder);
            stModelId.setModel(model);
            // Build stakeholder
            ModelProvider stModel = new ModelProvider();
            stModel.setId(stModelId);
            stModel.setModelValue(stakeholderModel.getModelValue());
            return stModel;
        }).forEach((stModel) -> {
            // Add stakeholder to the set
            stakeholders.add(stModel);
        });

        return stakeholders;
    }

    private SetRevenueShareConf fillRSModelInfo(RSSModel rssModel,
            SetRevenueShareConf model) {

        model.setAlgorithmType(rssModel.getAlgorithmType());

        // Create aggregator sharing object
        DbeAggregator aggregator = this.aggregatorDao.getById(rssModel.getAggregatorId());

        model.setAggregator(aggregator);
        model.setAggregatorValue(rssModel.getAggregatorValue());

        model.setOwnerValue(rssModel.getOwnerValue());

        // Set stakeholders
        if (rssModel.getStakeholders() != null) {
            Set<ModelProvider> stakeholders = this.buildStakeholdersModel(rssModel, model);

            model.setStakeholders(stakeholders);
        }
        return model;
    }

    private SetRevenueShareConf buildRSModel(RSSModel rssModel) {

        SetRevenueShareConfId id = this.buildRSModelId(rssModel);

        // Create new model
        SetRevenueShareConf model = new SetRevenueShareConf();
        model.setId(id);

        return this.fillRSModelInfo(rssModel, model);
    }

    /**
     * Creates a new RS Model.
     * 
     * @param rssModel
     * @return
     * @throws RSSException
     */
    public RSSModel createRssModel(RSSModel rssModel) throws RSSException {
        logger.debug("Into createRssModel() method");

        // check valid rssModel
        checkValidRSSModel(rssModel);

        List <SetRevenueShareConf> prevModels = this.revenueShareConfDao
                .getRevenueModelsByParameters(
                        rssModel.getAggregatorId(), rssModel.getOwnerProviderId(), rssModel.getProductClass());

        if (prevModels != null && prevModels.size() > 0) {
            String[] args = {"A model with the same Product Class already exists"};
            throw new RSSException(UNICAExceptionType.RESOURCE_ALREADY_EXISTS, args);
        }

        // Build database model for RS Model
        SetRevenueShareConf model = this.buildRSModel(rssModel);

        if (model.getStakeholders() == null) {
            model.setStakeholders(new HashSet<>());
        }

        Set<SetRevenueShareConf> models = model.getId().getModelOwner().getModels();

        if (null == models) {
            models = new HashSet<>();
        }
        models.add(model);

        // Persist models in the database
        // Save new RS model into database
        this.revenueShareConfDao.create(model);

        // Save model provider relationships for stakeholders
        model.getStakeholders().stream().forEach((st) -> {
            this.modelProviderDao.create(st);
        });
        // return model
        return rssModel;
    }

    /**
     * Update RSS model.
     * 
     * @param rssModel
     * @return
     * @throws Exception
     */
    public RSSModel updateRssModel(RSSModel rssModel) throws Exception {
        logger.debug("Into updateRssModel() method");
        // check valid rssModel
        checkValidRSSModel(rssModel);

        // Get exisintg RS model
        SetRevenueShareConfId id = this.buildRSModelId(rssModel);
        SetRevenueShareConf model = revenueShareConfDao.getById(id);

        // Check if the model does not exists
        if (null == model) {
            String[] args = {"RSS Model"};
            throw new RSSException(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, args);
        }

        // Update stakeholders
        Set<ModelProvider> oldSt = model.getStakeholders();
        model.setStakeholders(null);

        oldSt.stream().forEach((st) -> {
            this.modelProviderDao.delete(st);
        });

        // Save model into database
        revenueShareConfDao.update(this.fillRSModelInfo(rssModel, model));

        // Save model provider relationships for stakeholders
        model.getStakeholders().stream().forEach((st) -> {
            this.modelProviderDao.create(st);
        });
        // return model
        return rssModel;
    }

    /**
     * Delete RSS models.
     * 
     * @param aggregatorId
     * @param appProviderId
     * @param productClass
     * @throws Exception
     */
    public void deleteRssModel(String aggregatorId, String appProviderId, String productClass) throws Exception {
        logger.debug("Into deleteRssModel() method");

        // check valid appProvider
        if (null == aggregatorId || aggregatorId.trim().isEmpty()) {
            String[] args = {"aggregatorId"};
            throw new RSSException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, args);
        }

        if (appProviderId != null && !appProviderId.trim().isEmpty()) {
            checkValidAppProvider(aggregatorId, appProviderId);
        }

        // Get models
        List<SetRevenueShareConf> result = revenueShareConfDao.getRevenueModelsByParameters(aggregatorId,
            appProviderId, productClass);

        // Remove models
        if (null != result && !result.isEmpty()) {
            result.stream().map((model) -> {
                // Remove Stakeholders
                if (null != model.getStakeholders()) {
                    model.getStakeholders().stream().forEach((st) -> {
                        modelProviderDao.delete(st);
                    });
                }
                return model;
            }).forEach((model) -> {
                revenueShareConfDao.delete(model);
            });
        }
    }

    /**
     * Check that a appProviderId is valid for the current aggregator.
     * 
     * @param aggregatorId
     * @param appProviderId
     * @throws RSSException
     */
    public void checkValidAppProvider(String aggregatorId, String appProviderId)
            throws RSSException {
        logger.debug("Into checkValidAppProvider mehtod : aggregator:{} provider:{}", aggregatorId, appProviderId);

        DbeAppProvider provider = appProviderDao.getProvider(aggregatorId, appProviderId);
        if (null == provider) {
            String[] args = {"provider"};
            throw new RSSException(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, args);
        }
    }

    private void checkField (String field, String name) throws RSSException{
        if (null == field || field.isEmpty()) {
            String[] args = {name};
            throw new RSSException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, args);
        }
    }

    private void checkNumberField(BigDecimal number, String name) throws RSSException{
        if (null == number) {
            String[] args = {name};
            throw new RSSException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, args);
        }
    }

    /**
     * Check that a RssModels contains all required Information.
     * 
     * @param rssModel
     * @throws RSSException
     */
    public void checkValidRSSModel(RSSModel rssModel) throws RSSException {
        logger.debug("Into checkValidRSSModel mehtod");

        // Validate basic fields
        this.checkField(rssModel.getAggregatorId(), "aggregatorId");
        this.checkField(rssModel.getOwnerProviderId(), "ownerProviderId");

        this.checkNumberField(rssModel.getOwnerValue(), "ownerValue");

        this.checkField(rssModel.getAlgorithmType(), "algorithmType");
        this.checkNumberField(rssModel.getAggregatorValue(), "aggregatorValue");

        this.checkField(rssModel.getProductClass(), "productClass");
        // Check valid provider owner
        this.checkValidAppProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());

        // Check stakeholders fields if existing
        if (rssModel.getStakeholders() != null) {
            for (StakeholderModel stModel: rssModel.getStakeholders()) {
                this.checkField(stModel.getStakeholderId(), "stakeholderId");
                this.checkNumberField(stModel.getModelValue(), "modelValue");

                this.checkValidAppProvider(rssModel.getAggregatorId(), stModel.getStakeholderId());

                // Check that the stakeholder is not the owner provider
                if (stModel.getStakeholderId().equalsIgnoreCase(rssModel.getOwnerProviderId())) {
                    String[] args = {"The RS model owner cannot be included as stakeholder"};
                    throw new RSSException(UNICAExceptionType.INVALID_PARAMETER, args);
                }
            }
        }

        // Check algorithm specific restrictions
        AlgorithmFactory algorithmFactory = new AlgorithmFactory();
        AlgorithmProcessor processor = algorithmFactory.
                getAlgorithmProcessor(rssModel.getAlgorithmType());

        processor.validateModel(rssModel);
    }

    /**
     * Converts the model from objects to the bean that is serialized by the API.
     * 
     * @param model
     * @return
     */
    public RSSModel convertIntoApiModel(SetRevenueShareConf model) {
        RSSModel rssModel = new RSSModel();
        // Fill basic revenue sharing model info
        rssModel.setOwnerProviderId(
                model.getId().getModelOwner().
                        getId().getTxAppProviderId()
        );
        rssModel.setOwnerValue(model.getOwnerValue());
        rssModel.setAggregatorId(model.getAggregator().getTxEmail());
        rssModel.setAggregatorShare(model.getAggregatorValue());
        rssModel.setAlgorithmType(model.getAlgorithmType());
        rssModel.setProductClass(model.getId().getProductClass());

        // Fill stakeholders list
        List<StakeholderModel> stakeholdersList = new ArrayList<>();

        model.getStakeholders().stream().map((stk) -> {
            StakeholderModel stModel = new StakeholderModel();
            stModel.setStakeholderId(stk.getStakeholder().getId().getTxAppProviderId());
            stModel.setModelValue(stk.getModelValue());
            return stModel;
        }).forEach((stModel) -> {
            stakeholdersList.add(stModel);
        });
        rssModel.setStakeholders(stakeholdersList);
        return rssModel;
    }
}
