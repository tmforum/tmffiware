# -*- coding: utf-8 -*-

# Copyright (c) 2015 - 2016 CoNWeT Lab., Universidad Politécnica de Madrid

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

from functools import wraps

from wstore.models import ResourcePlugin
from wstore.asset_manager.models import Resource
from wstore.asset_manager.errors import ProductError


def _get_plugin_model(name):
    try:
        plugin_model = ResourcePlugin.objects.get(name=name)
    except:
        # Validate resource type
        raise ProductError('The given product specification contains a not supported asset type: ' + name)

    return plugin_model


def load_plugin_module(asset_t):
    module = _get_plugin_model(asset_t).module
    module_class_name = module.split('.')[-1]
    module_package = module.partition('.' + module_class_name)[0]

    module_class = getattr(__import__(module_package, globals(), locals(), [module_class_name], -1), module_class_name)

    return module_class()


def on_product_spec_validation(func):

    @wraps(func)
    def wrapper(self, provider, asset_t, media_type, url):

        plugin_module = load_plugin_module(asset_t)

        # On pre validation
        plugin_module.on_pre_product_spec_validation(provider, asset_t, media_type, url)

        # Call method
        asset = func(self, provider, asset_t, media_type, url)

        # On post validation
        plugin_module.on_post_product_spec_validation(provider, asset)

        return asset

    return wrapper


def on_product_spec_attachment(func):

    @wraps(func)
    def wrapper(self, asset, asset_t, product_spec):

        # Load plugin module
        plugin_module = load_plugin_module(asset_t)

        # Call on pre create event handler
        plugin_module.on_pre_product_spec_attachment(asset, asset_t, product_spec)

        # Call method
        func(self, asset, asset_t, product_spec)

        # Call on post create event handler
        plugin_module.on_post_product_spec_attachment(asset, asset_t, product_spec)

    return wrapper


def on_product_offering_validation(func):

    @wraps(func)
    def wrapper(self, provider, product_offering):

        # Get the related asset (the existence of the product has been already validated)
        assets = Resource.objects.filter(product_id=product_offering['productSpecification']['id'])

        if len(assets):
            plugin_module = load_plugin_module(assets[0].resource_type)

            plugin_module.on_pre_product_offering_validation(assets[0], product_offering)

        func(self, provider, product_offering)

        if len(assets):
            plugin_module.on_post_product_offering_validation(assets[0], product_offering)

    return wrapper


def on_product_acquired(order, contract):
    # Get digital asset from the contract
    if contract.offering.is_digital:
        asset = contract.offering.asset

        # Load plugin module
        plugin_module = load_plugin_module(asset.resource_type)

        # Execute event
        plugin_module.on_product_acquisition(asset, contract, order)

