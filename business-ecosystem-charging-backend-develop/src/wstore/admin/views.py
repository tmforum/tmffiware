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

from django.http import HttpResponse

from wstore.store_commons.utils.http import build_response, supported_request_mime_types, \
    authentication_required
from wstore.store_commons.resource import Resource
from wstore.store_commons.utils.name import is_valid_id
from wstore.models import Context


class CurrencyCollection(Resource):

    @authentication_required
    def read(self, request):

        # Get Store context
        context = Context.objects.all()[0]
        # Return currencies
        if 'allowed' in context.allowed_currencies:
            currs = [{
                'currency': curr['currency'],
                'default': curr['currency'].lower() == context.allowed_currencies['default'].lower()
            } for curr in context.allowed_currencies['allowed']]
        else:
            currs = []

        response = json.dumps(currs)

        return HttpResponse(response, status=200, mimetype='application/json; charset=utf-8')

    @authentication_required
    @supported_request_mime_types(('application/json',))
    def create(self, request):
        # Check if the user is an admin
        if not request.user.is_staff:
            return build_response(request, 403, 'Forbidden')

        # Get data
        data = json.loads(request.body)
        if not 'currency' in data:
            return build_response(request, 400, 'Invalid JSON content')

        # Check currency regex
        if not is_valid_id(data['currency']):
            return build_response(request, 400, 'Invalid currency format')

        # Get the context
        context = Context.objects.all()[0]

        # Check if the allowed_currencies list is empty
        first = False
        if not 'allowed' in context.allowed_currencies:
            first = True
            context.allowed_currencies['allowed'] = []

        error = False
        # Check that the new currency is not already included
        for curr in context.allowed_currencies['allowed']:
            if curr['currency'].lower() == data['currency'].lower():
                error = True
                break

        if error:
            return build_response(request, 400, 'The currency already exists')

        # Check if it is the default currency
        if ('default' in data and data['default']) or first:
            # Note the this will override previous default currency
            context.allowed_currencies['default'] = data['currency']

        # Include new currency
        context.allowed_currencies['allowed'].append({
            'currency': data['currency'],
            'in_use': False
        })
        context.save()
        return build_response(request, 201, 'Created')


class CurrencyEntry(Resource):

    @authentication_required
    def update(self, request, currency):
        """
        This method is used to change the default currency
       """
        if not request.user.is_staff:
            build_response(request, 403, 'Forbidden')

        # Get the context
        context = Context.objects.all()[0]

        # Check that the currency exist
        if not 'default' in context.allowed_currencies:
            return build_response(request, 404, 'Not found')

        if not currency.lower() == context.allowed_currencies['default'].lower():
            for c in context.allowed_currencies['allowed']:
                if c['currency'].lower() == currency.lower():
                    break
            else:
                return build_response(request, 404, 'Not found')

        # Make the currency the default currency
        context.allowed_currencies['default'] = currency
        context.save()

        # Return response
        return build_response(request, 200, 'OK')

    @authentication_required
    def delete(self, request, currency):

        # Check if the user is an admin
        if not request.user.is_staff:
            return build_response(request, 403, 'Forbidden')

        # Get the context
        context = Context.objects.all()[0]

        if not 'allowed' in context.allowed_currencies:
            return build_response(request, 404, 'Not found')

        del_curr = None
        # Search the currency
        for curr in context.allowed_currencies['allowed']:
            if curr['currency'].lower() == currency.lower():
                del_curr = curr
                break
        else:
            return build_response(request, 404, 'Not found')

        # Check if is possible to delete the currency
        if  context.allowed_currencies['default'].lower() == currency.lower():
            return build_response(request, 403, 'The currency cannot be deleted: It is the default currency')

        if del_curr['in_use']:
            return build_response(request, 403, 'The currency cannot be deleted: An offering is using it')

        # Remove the currency
        context.allowed_currencies['allowed'].remove(del_curr)
        context.save()

        return build_response(request, 204, 'No content')
