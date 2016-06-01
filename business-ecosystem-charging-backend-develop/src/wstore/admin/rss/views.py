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

from __future__ import unicode_literals

import json
from decimal import Decimal

from django.http import HttpResponse

from wstore.store_commons.resource import Resource
from wstore.store_commons.utils.http import build_response, supported_request_mime_types, \
    authentication_required, identity_manager_required
from wstore.admin.rss.models import RevenueModel
from wstore.models import Context


def _check_revenue_models(models):
    """
    Check the revenue sharing models provided in the request
    """

    fixed_models = []
    found_models = []
    for model in models:
        # Check model contents
        if 'class' not in model or 'percentage' not in model:
            raise ValueError('Invalid revenue sharing model: Missing a required field')

        # Check product class
        if model['class'] != 'one-time' and model['class'] != 'recurring' and model['class'] != 'usage':
            raise ValueError('Invalid revenue sharing model: Invalid product class')
        else:
            found_models.append(model['class'])

        # Check percentage type
        try:
            model['percentage'] = Decimal(model['percentage'])
        except:
            raise TypeError('Invalid revenue sharing model: Invalid percentage type')

        # Check percentage value
        if model['percentage'] < Decimal(0) or model['percentage'] > Decimal(100):
            raise ValueError('Invalid revenue sharing model: The percentage must be a number between 0 and 100')

        fixed_models.append(model)

    # Check that a percentage has been included for every needed product class
    if len(found_models) != 3 or 'one-time' not in found_models \
            or 'recurring' not in found_models or 'usage' not in found_models:
        raise ValueError('Invalid revenue sharing model: Missing a required product class')

    return fixed_models


def build_db_models(sharing_models):
    # Build revenue models
    db_revenue_models = []
    for model in sharing_models:
        db_revenue_models.append(RevenueModel(
            revenue_class=model['class'],
            percentage=model['percentage']
        ))
    return db_revenue_models


class RSSCollection(Resource):

    @identity_manager_required
    @authentication_required
    def read(self, request):
        context = Context.objects.all()[0]
        response = [{'revenue_class': model.revenue_class, 'percentage': unicode(model.percentage)} for model in context.revenue_models]

        return HttpResponse(json.dumps(response), status=200, mimetype='application/json')

    @authentication_required
    @identity_manager_required
    @supported_request_mime_types(('application/json',))
    def update(self, request):

        # Only the admin can register new RSS instances
        if not request.user.is_staff:
            return build_response(request, 403, 'Forbidden')

        data = json.loads(request.body)

        # Check revenue sharing models
        if 'models' in data:
            try:
                sharing_models = _check_revenue_models(data['models'])
            except Exception as e:
                return build_response(request, 400, unicode(e))
        context = Context.objects.all()[0]

        context.revenue_models = build_db_models(sharing_models)
        context.save()

        return build_response(request, 201, 'Created')
