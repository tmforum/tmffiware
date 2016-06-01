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

import requests

from wstore.asset_manager.catalog_validator import CatalogValidator
from wstore.asset_manager.models import Resource
from wstore.store_commons.utils.units import recurring_periods, supported_currencies
from wstore.asset_manager.resource_plugins.decorators import on_product_offering_validation


class OfferingValidator(CatalogValidator):

    def _update_product_id(self, product_url):
        # Complete asset info with product spec id
        r = requests.get(product_url)
        product_info = r.json()

        asset_t, media_type, url = self.parse_characteristics(product_info)

        if asset_t is not None and media_type is not None and url is not None:
            asset = Resource.objects.get(download_link=url)
            asset.product_id = product_info['id']
            asset.save()

    @on_product_offering_validation
    def _validate_offering_pricing(self, provider, product_offering):
        # Validate offering pricing fields
        if 'productOfferingPrice' in product_offering:
            for price_model in product_offering['productOfferingPrice']:

                # Validate price unit
                if 'priceType' not in price_model:
                    raise ValueError('Missing required field priceType in productOfferingPrice')

                if price_model['priceType'] != 'one time' and price_model['priceType'] != 'recurring' and price_model['priceType'] != 'usage':
                    raise ValueError('Invalid priceType, it must be one time, recurring, or usage')

                if price_model['priceType'] == 'recurring' and 'recurringChargePeriod' not in price_model:
                    raise ValueError('Missing required field recurringChargePeriod for recurring priceType')

                if price_model['priceType'] == 'recurring' and price_model['recurringChargePeriod'].lower() not in recurring_periods:
                    raise ValueError('Unrecognized recurringChargePeriod: ' + price_model['recurringChargePeriod'])

                # Validate currency
                if 'price' not in price_model:
                    raise ValueError('Missing required field price in productOfferingPrice')

                if 'currencyCode' not in price_model['price']:
                    raise ValueError('Missing required field currencyCode in price')

                if price_model['price']['currencyCode'] not in supported_currencies:
                    raise ValueError('Unrecognized currency: ' + price_model['price']['currencyCode'])

    def validate_creation(self, provider, product_offering):
        self._update_product_id(product_offering['productSpecification']['href'])
        self._validate_offering_pricing(provider, product_offering)

