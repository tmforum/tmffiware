# -*- coding: utf-8 -*-

# Copyright (c) 2016 CoNWeT Lab., Universidad Politécnica de Madrid

# This file is part of WStore.

# WStore is free software: you can redistribute it and/or modify
# it under the terms of the European Union Public Licence (EUPL)
# as published by the European Commission, either version 1.1
# of the License, or (at your option) any later version.

# WStore is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# European Union Public Licence for more details.

# You should have received a copy of the European Union Public Licence
# along with WStore.
# If not, see <https://joinup.ec.europa.eu/software/page/eupl/licence-eupl>.

from __future__ import unicode_literals

recurring_periods = {
    'daily': 1,  # One day
    'weekly': 7,  # One week
    'monthly': 30,  # One month
    'quarterly': 90,  # Three months
    'yearly': 365,  # One year
    'quinquennial': 1825  # Five years
}

supported_currencies = (
    'AUD', 'BRL', 'CAD', 'CZK', 'DKK',
    'EUR', 'HKD', 'HUF', 'ILS', 'JPY',
    'MYR', 'MXN', 'TWD', 'NZD', 'NOK',
    'PHP', 'PLN', 'GBP', 'RUB', 'SGD',
    'SEK', 'CHF', 'THB', 'TRY', 'USD'
)