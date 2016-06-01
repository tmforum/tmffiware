# -*- coding: utf-8 -*-

# Copyright (c) 2013 - 2015 CoNWeT Lab., Universidad Politécnica de Madrid

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
from urlparse import urljoin

from django.contrib.auth.models import User
from django.http import HttpResponse

from wstore.store_commons.utils.http import build_response, supported_request_mime_types, \
    authentication_required
from wstore.store_commons.resource import Resource
from wstore.models import Context
from wstore.models import Organization


class UserProfileEntry(Resource):

    @authentication_required
    def read(self, request, username):

        if not request.user.is_staff and not request.user.username == username:
            return build_response(request, 403, 'You are not authorized to retrieve user info')

        # Get user info
        user = User.objects.get(username=username)
        profile = user.userprofile

        site = Context.objects.all()[0].site.domain

        user_profile = {
            'href': urljoin(site, 'charging/api/userManagement/users/' + username),
            'id': user.username,
            'completeName': profile.complete_name,
            'currentOrganization': profile.current_organization.name,
            'organizations': []
        }

        # Include organizations name
        for o in profile.organizations:
            org = Organization.objects.get(pk=o['organization'])

            org_info = {
                'name': org.name,
                'roles': o['roles']
            }

            if user.pk in org.managers and not profile.is_user_org():
                org_info['roles'].append('manager')

            user_profile['organizations'].append(org_info)

        user_profile['billingAddress'] = profile.current_organization.tax_address

        # Include roles for the user
        user_profile['currentRoles'] = profile.get_current_roles()

        if user.is_staff:
            user_profile['currentRoles'].append('admin')

        return HttpResponse(json.dumps(user_profile), status=200, mimetype='application/json')

    @authentication_required
    @supported_request_mime_types(('application/json',))
    def patch(self, request, username):

        if not request.user.is_staff and not request.user.username == username:
            return build_response(request, 403, 'You are not authorized to update user info')

        try:
            data = json.loads(request.body)
        except:
            return build_response(request, 400, 'Invalid JSON content')

        # Get user org for storing the billing address
        user_org = Organization.objects.get(name=username)

        if 'billingAddress' in data:
            # Check that the billing information is correct
            if 'street' in data['billingAddress']:
                user_org.tax_address['street'] = data['billingAddress']['street']

            if 'postal' in data['billingAddress']:
                user_org.tax_address['postal'] = data['billingAddress']['postal']

            if 'city' in data['billingAddress']:
                user_org.tax_address['city'] = data['billingAddress']['city']

            if 'province' in data['billingAddress']:
                user_org.tax_address['province'] = data['billingAddress']['province']

            if 'country' in data['billingAddress']:
                user_org.tax_address['country'] = data['billingAddress']['country']

            if 'street' not in user_org.tax_address or 'postal' not in user_org.tax_address or\
                    'city' not in user_org.tax_address or 'province' not in user_org.tax_address or\
                    'country' not in user_org.tax_address:
                return build_response(request, 400, 'Incomplete billing address, there is a missing field')

            user_org.save()

        return build_response(request, 200, 'OK')
