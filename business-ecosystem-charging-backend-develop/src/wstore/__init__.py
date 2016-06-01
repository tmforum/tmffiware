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

import sys

from django.conf import settings

from wstore.models import Context
from wstore.ordering.inventory_client import InventoryClient
from wstore.rss_adaptor.rss_manager import ProviderManager


testing = sys.argv[1:2] == ['test']

if not testing and Context.objects.all():
    inventory = InventoryClient()
    inventory.create_inventory_subscription()

    # Create RSS default aggregator and provider
    credentials = {
        'user': settings.STORE_NAME,
        'roles': ['provider'],
        'email': settings.WSTOREMAIL
    }
    prov_manager = ProviderManager(credentials)

    try:
        prov_manager.register_aggregator({
            'aggregatorId': settings.WSTOREMAIL,
            'aggregatorName': settings.STORE_NAME,
            'defaultAggregator': True
        })
    except Exception as e:  # If the error is a conflict means that the aggregator is already registered
        if e.response.status_code != 409:
            raise e
