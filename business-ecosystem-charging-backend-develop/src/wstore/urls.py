# -*- coding: utf-8 -*-

# Copyright (c) 2013 - 2016 CoNWeT Lab., Universidad Politécnica de Madrid

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

from django.conf.urls import patterns, url

from wstore.admin.users import views as user_views
from wstore.asset_manager import views as offering_views
from wstore.asset_manager.resource_plugins import views as plugins_views
from wstore.ordering import views as ordering_views
from wstore.charging_engine import views as charging_views

urlpatterns = patterns('',
    # API
    url(r'^charging/api/userManagement/users/(?P<username>[\w -]+)/?$', user_views.UserProfileEntry(permitted_methods=('GET', 'PATCH'))),
    url(r'^charging/api/assetManagement/assets/uploadJob/?$', offering_views.UploadCollection(permitted_methods=('POST',))),
    url(r'^charging/api/assetManagement/assets/validateJob/?$', offering_views.ValidateCollection(permitted_methods=('POST',))),
    url(r'^charging/api/assetManagement/assets/offeringJob/?$', offering_views.ValidateOfferingCollection(permitted_methods=('POST',))),
    url(r'^charging/api/assetManagement/assets/?$', offering_views.AssetCollection(permitted_methods=('GET',))),
    url(r'^charging/api/assetManagement/assets/(?P<asset_id>\w+)/?$', offering_views.AssetEntry(permitted_methods=('GET',))),
    url(r'^charging/api/assetManagement/assetTypes/?$', plugins_views.PluginCollection(permitted_methods=('GET', ))),
    url(r'^charging/api/assetManagement/assetTypes/(?P<plugin_id>[\w -]+)/?$', plugins_views.PluginEntry(permitted_methods=('GET',))),

    url(r'^charging/api/orderManagement/orders/?$', ordering_views.OrderingCollection(permitted_methods=('POST',))),
    url(r'^charging/api/orderManagement/products/?$', ordering_views.InventoryCollection(permitted_methods=('POST',))),
    url(r'^charging/api/orderManagement/products/renewJob/?$', ordering_views.RenovationCollection(permitted_methods=('POST',))),
    url(r'^charging/api/orderManagement/orders/accept/?$', charging_views.PayPalConfirmation(permitted_methods=('POST',))),
    url(r'^charging/api/orderManagement/orders/cancel/?$', charging_views.PayPalCancellation(permitted_methods=('POST',))),
    url(r'^charging/api/orderManagement/orders/refund/?$', charging_views.PayPalRefund(permitted_methods=('POST',))),
    url(r'^charging/api/orderManagement/accounting/?$', charging_views.ServiceRecordCollection(permitted_methods=('POST',)))
)
