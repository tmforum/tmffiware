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
from datetime import datetime
from urlparse import urljoin

from django.core.exceptions import ImproperlyConfigured
from django.conf import settings

from wstore.models import Context


class InventoryClient:

    def __init__(self):
        self._inventory_api = settings.INVENTORY

    def _build_callback_url(self):
        # Use the local site for registering the callback
        site = Context.objects.all()[0].local_site.domain

        return urljoin(site, 'charging/api/orderManagement/products')

    def get_hubs(self):
        r = requests.get(self._inventory_api + '/api/productInventory/v2/hub')
        r.raise_for_status()
        return r.json()

    def create_inventory_subscription(self):
        """
        Creates a subscription to the inventory API so the server will be able to activate products
        """

        callback_url = self._build_callback_url()

        for hub in self.get_hubs():
            if hub['callback'] == callback_url:
                break
        else:
            callback = {
                'callback': callback_url
            }

            r = requests.post(self._inventory_api + '/api/productInventory/v2/hub', json=callback)

            if r.status_code != 201 and r.status_code != 409:
                msg = "It hasn't been possible to create inventory subscription, "
                msg += 'please check that the inventory API is correctly configured '
                msg += 'and that the inventory API is up and running'
                raise ImproperlyConfigured(msg)

    def get_product(self, product_id):
        url = self._inventory_api + '/api/productInventory/v2/product/' + unicode(product_id)

        r = requests.get(url)
        r.raise_for_status()

        return r.json()

    def activate_product(self, product_id):
        """
        Activates a given product by changing its state to Active and providing a startDate
        :param product_id: Id of the product to be activated
        """
        # Build product url
        url = self._inventory_api + '/api/productInventory/v2/product/' + unicode(product_id)
        patch_body = {
            'status': 'Active',
            'startDate': unicode(datetime.now()).replace(' ', 'T')
        }

        r = requests.patch(url, json=patch_body)
        r.raise_for_status()

    def suspend_product(self, product_id):
        """
        Suspends a given product by changing its state to Suspended
        :param product_id: Id of the product to be suspended
        """
        # Build product url
        url = self._inventory_api + '/api/productInventory/v2/product/' + unicode(product_id)
        patch_body = {
            'status': 'Suspended'
        }

        r = requests.patch(url, json=patch_body)
        r.raise_for_status()

    def terminate_product(self, product_id):
        """
        terminates a given product by changing its state to Terminated
        :param product_id: Id of the product to be terminated
        """

        # Activate the product since it must be in active state to be terminated
        try:
            self.activate_product(product_id)
        except:
            pass

        # Build product url
        url = self._inventory_api + '/api/productInventory/v2/product/' + unicode(product_id)
        patch_body = {
            'status': 'Terminated',
            'terminationDate': unicode(datetime.now()).replace(' ', 'T')
        }

        r = requests.patch(url, json=patch_body)
        r.raise_for_status()
