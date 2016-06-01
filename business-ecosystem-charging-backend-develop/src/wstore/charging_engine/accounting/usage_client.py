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
from urlparse import urljoin

from django.conf import settings

from wstore.charging_engine.accounting.errors import UsageError
from wstore.models import Context


class UsageClient:

    def __init__(self):
        self._usage_api = settings.USAGE
        if not self._usage_api.endswith('/'):
            self._usage_api += '/'

    def _validate_state(self, state):
        valid_states = ['Guided', 'Rated', 'Rejected', 'Billed']

        if state not in valid_states:
            raise UsageError('Invalid usage status ' + state)

    def _belongs_to_product(self, usage, product_id):
        belongs = False
        for char in usage['usageCharacteristic']:
            if char['name'].lower() == 'productid' and char['value'] == product_id:
                belongs = True
                break

        return belongs

    def get_customer_usage(self, customer, product_id, state=None):
        """
        Retrieves the usage made by a customer filtered by service and status
        :param customer: username of the customer
        :param product_id: id of the acquired product being used
        :param state: state of the usage to be retrieved
        :return: List of customer usages
        """
        # Get customer usage filtered by state
        path = 'api/usageManagement/v2/usage'
        url = urljoin(self._usage_api, path) + '?relatedParty.id=' + customer

        if state is not None:
            self._validate_state(state)
            url += '&status=' + state

        r = requests.get(url, headers={
            'Accept': 'application/json'
        })

        r.raise_for_status()

        raw_usage = r.json()
        # Filter only the usage belonging to the specified product
        return [usage_doc for usage_doc in raw_usage if self._belongs_to_product(usage_doc, product_id)]

    def _patch_usage(self, usage_id, patch):
        path = 'api/usageManagement/v2/usage/' + unicode(usage_id)
        url = urljoin(self._usage_api, path)

        r = requests.patch(url, json=patch)
        r.raise_for_status()

    def update_usage_state(self, usage_id, state):
        """
        Updates the status of a given usage
        :param usage_id: usage to be modified
        :param state: new status
        """
        self._validate_state(state)

        # Update document state
        patch = {
            'status': state
        }
        self._patch_usage(usage_id, patch)

    def rate_usage(self, usage_id, timestamp, duty_free, price, rate, currency, product_id):
        """
        Rates a product with the amount to be charge to the customer based on the given usage
        :param usage_id: usage where the rate is going to be included
        :param timestamp: Timestamp when the used was rated
        :param duty_free: rate value without taxes
        :param price: rate value with taxes
        :param rate: applied tax rate
        :param currency: currency of the amount
        :param product_id: Id of the product that generates the usage
        :return:
        """
        inventory_path = settings.INVENTORY.split('/')[3]
        ext_host = Context.objects.all()[0].site.domain
        inventory_url = urljoin(ext_host, inventory_path + '/')

        product_url = urljoin(inventory_url, 'api/productInventory/v2/product/' + unicode(product_id))
        patch = {
            'status': 'Rated',
            'ratedProductUsage': [{
                'ratingDate': timestamp.replace(' ', 'T'),
                'usageRatingTag': 'usage',
                'isBilled': False,
                'ratingAmountType': 'Total',
                'taxIncludedRatingAmount': price,
                'taxExcludedRatingAmount': duty_free,
                'taxRate': rate,
                'isTaxExempt': False,
                'offerTariffType': 'Normal',
                'currencyCode': currency,
                'productRef': product_url
            }]
        }

        self._patch_usage(usage_id, patch)
