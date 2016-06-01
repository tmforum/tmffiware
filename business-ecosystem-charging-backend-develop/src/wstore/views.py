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

import os

from django.conf import settings
from django.utils.encoding import smart_str
from django.views.static import serve
from django.http import HttpResponse

from store_commons.utils.http import build_response
from wstore.store_commons.resource import Resource as API_Resource

from wstore.models import Resource
from wstore.ordering.models import Order, Offering


class ServeMedia(API_Resource):

    def _validate_asset_permissions(self, user, path, name):
        err_code, err_msg = None, None

        # Retrieve the given digital asset
        try:
            resource_path = os.path.join(settings.MEDIA_DIR, path, name)
            asset = Resource.objects.get(resource_path=resource_path)
        except:
            err_code, err_msg = 404, 'The specified asset does not exists'

        # Check if the user has permissions to download the asset
        if err_code is None and not asset.is_public:
            if user.is_anonymous():
                err_code, err_msg = 401, 'You must be authenticated to download the specified asset'

            if err_code is None and user.userprofile.current_organization != asset.provider:
                # Check if the user has acquired the asset
                for off in user.userprofile.current_organization.acquired_offerings:
                    offering = Offering.objects.get(pk=off)
                    if offering.asset == asset:
                        break
                else:
                    err_code, err_msg = 403, 'You are not authorized to download the specified asset'

        return err_code, err_msg

    def _validate_invoice_permissions(self, user, name):
        err_code, err_msg = None, None

        if user.is_anonymous():
            err_code, err_msg = 401, 'You must provide credentials for downloading invoices'
        else:
            try:
                order = Order.objects.get(pk=name[:24])
            except:
                err_code, err_msg = 404, 'The specified invoice does not exists'

            if err_code is None and order.owner_organization != user.userprofile.current_organization:
                err_code, err_msg = 403, 'You are not authorized to download the specified invoice'

        return err_code, err_msg

    def read(self, request, path, name):
        # Protect the resources from not authorized downloads
        if path.startswith('assets'):
            err_code, err_msg = self._validate_asset_permissions(request.user, path, name)
        elif path.startswith('bills'):
            err_code, err_msg = self._validate_invoice_permissions(request.user, name)
        else:
            err_code, err_msg = 404, 'Resource not found'

        local_path = os.path.join(path, name)
        if err_code is None and not os.path.isfile(os.path.join(settings.MEDIA_ROOT, local_path)):
            err_code, err_msg = 404, 'Resource not found'

        if err_code is not None:
            response = build_response(request, err_code, err_msg)
        elif not getattr(settings, 'USE_XSENDFILE', False):
            response = serve(request, local_path, document_root=settings.MEDIA_ROOT)
        else:
            response = HttpResponse()
            response['X-Sendfile'] = smart_str(local_path)

        return response
