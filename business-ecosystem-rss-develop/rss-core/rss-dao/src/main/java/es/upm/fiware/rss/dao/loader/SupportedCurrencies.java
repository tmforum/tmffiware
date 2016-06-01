/**
 * Copyright (C) 2016 CoNWeT Lab., Universidad Politécnica de Madrid
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

/**
 *
 * @author fdelavega
 */
public enum SupportedCurrencies {
    // Current supported currencies
    AUD("Australian dollar", "$"),
    BRL("Brazilian real", "R$"),
    CAD("Canadian dollar", "$"),
    CZK("Czech koruna", "Kč"),
    DKK("Danish krone", "kr"),
    EUR("Euro", "€"),
    HKD("Hong Kong dollar", "$"),
    HUF("Hungarian forint", "Ft"),
    ILS("Israeli new shekel", "₪"),
    JPY("Japanese yen", "¥"),
    MYR("Malaysian ringgit", "RM"),
    MXN("Mexican peso", "$"),
    TWD("New Taiwan dollar", "NT$"),
    NZD("New Zealand dollar", "NZ$"),
    NOK("Norwegian krone", "kr"),
    PHP("Philippine peso", "₱"),
    PLN("Polish złoty", "zł"),
    GBP("Pound sterling", "£"),
    RUB("Russian ruble", "RUB"),
    SGD("Singapore dollar", "S$"),
    SEK("Swedish krona", "kr"),
    CHF("Swiss franc", "Fr"),
    THB("Thai baht", "฿"),
    TRY("Turkish lira", "₺"),
    USD("United States dollar", "$");
    
    private final String description;
    private final String symbol;

    SupportedCurrencies(String description, String symbol) {
        this.description = description;
        this.symbol = symbol;
    }

    public String getDescription() {
        return description;
    }

    public String getSymbol() {
        return symbol;
    }
    
}
