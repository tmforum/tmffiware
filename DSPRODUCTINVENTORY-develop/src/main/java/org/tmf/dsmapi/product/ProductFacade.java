package org.tmf.dsmapi.product;

import java.util.Date;
import org.tmf.dsmapi.commons.facade.AbstractFacade;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.commons.exceptions.ExceptionType;
import org.tmf.dsmapi.commons.exceptions.UnknownResourceException;
import org.tmf.dsmapi.commons.utils.BeanUtils;
import org.tmf.dsmapi.product.model.Product;
import org.tmf.dsmapi.product.event.ProductEventPublisherLocal;
import org.tmf.dsmapi.product.model.State;

/**
 *
 * @author pierregauthier
 */
@Stateless
public class ProductFacade extends AbstractFacade<Product> {

    @PersistenceContext(unitName = "DSProductPU")
    private EntityManager em;
    @EJB
    ProductEventPublisherLocal publisher;
    StateModelImpl stateModel = new StateModelImpl();

    public ProductFacade() {
        super(Product.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public void checkCreation(Product newProduct) throws BadUsageException, UnknownResourceException {
        if (newProduct.getId() != null) {
            if (this.find(newProduct.getId()) != null) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC,
                        "Duplicate Exception, Product with same id :" + newProduct.getId() + " alreay exists");
            }
        }

        //verify first status
        if (null == newProduct.getStatus()) {
            newProduct.setStatus(State.Created);
        } else {
            if (!newProduct.getStatus().name().equalsIgnoreCase(State.Created.name())) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_FLOW_TRANSITION, "status " + newProduct.getStatus().value() + " is not the first state, attempt : " + State.Created.value());
            }
        }

    }

    public Product patchAttributs(long id, Product partialProduct) throws UnknownResourceException, BadUsageException {
        Product currentProduct = this.find(id);

        if (currentProduct == null) {
            throw new UnknownResourceException(ExceptionType.UNKNOWN_RESOURCE);
        }

        verifyStatus(currentProduct, partialProduct);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(partialProduct, JsonNode.class);
        partialProduct.setId(id);
        if (BeanUtils.patch(currentProduct, partialProduct, node)) {
            publisher.valueChangedNotification(currentProduct, new Date());
        }

        return currentProduct;
    }

    public void verifyStatus(Product currentProduct, Product partialProduct) throws BadUsageException {
        if (null != partialProduct.getStatus()) {
            stateModel.checkTransition(currentProduct.getStatus(), partialProduct.getStatus());
            publisher.statusChangedNotification(currentProduct, new Date());
        }
    }

}
