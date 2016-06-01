/**
 * Copyright (C) 2015 - 2016 CoNWeT Lab., Universidad Politécnica de Madrid
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
package es.upm.fiware.rss.dao.loader;

import es.upm.fiware.rss.dao.CurrencyDao;
import es.upm.fiware.rss.model.BmCurrency;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author fdelavega
 */

@Component
public class BmCurrencyLoader {

    @Autowired
    private CurrencyDao currencyDao;

    @Autowired
    @Qualifier("transactionManager")
    protected PlatformTransactionManager txManager;

    private void saveCurrency(String code, String description, String symbol) {
        BmCurrency c = new BmCurrency();
        c.setTxIso4217Code(code);
        c.setTxDescription(description);
        c.setTcSymbol(symbol);
        this.currencyDao.create(c);
    }

    @PostConstruct
    public void init() {
        TransactionTemplate tmpl = new TransactionTemplate(txManager);
        tmpl.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus ts) {
                // Check if currencies has been loaded
                List<BmCurrency> currs = currencyDao.getAll();

                if (currs == null || currs.isEmpty()) {
                    // Save supported currencies
                    for (SupportedCurrencies curr:  SupportedCurrencies.values()) {
                        saveCurrency(curr.toString(), curr.getDescription(), curr.getSymbol());
                    }
                }
            }
        });
    }
}
