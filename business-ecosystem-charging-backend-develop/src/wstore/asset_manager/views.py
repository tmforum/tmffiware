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
from django.core.exceptions import PermissionDenied, ObjectDoesNotExist

from wstore.store_commons.resource import Resource
from wstore.store_commons.utils.http import build_response, get_content_type, supported_request_mime_types, \
    authentication_required
from wstore.asset_manager.asset_manager import AssetManager
from wstore.asset_manager.product_validator import ProductValidator
from wstore.asset_manager.offering_validator import OfferingValidator
from wstore.store_commons.errors import ConflictError
from wstore.asset_manager.errors import ProductError


class AssetCollection(Resource):

    @authentication_required
    def read(self, request):
        """
        Retrives the existing digital assets associated with a given seller
        :param request:
        :return: JSON List containing the existing assets
        """

        pagination = {
            'start': request.GET.get('start', None),
            'limit': request.GET.get('limit', None)
        }
        if pagination['start'] is None or pagination['limit'] is None:
            pagination = None

        profile = request.user.userprofile

        if 'provider' not in profile.get_current_roles():
            return build_response(request, 403, 'You are not authorized to retrieve digital asset information')

        try:
            asset_manager = AssetManager()
            response = asset_manager.get_provider_assets_info(request.user, pagination=pagination)
        except Exception as e:
            return build_response(request, 400, unicode(e))

        return HttpResponse(json.dumps(response), status=200, mimetype='application/json; charset=utf-8')


class AssetEntry(Resource):

    @authentication_required
    def read(self, request, asset_id):
        """
        Retrieves the information associated to a given digital asset
        :param request:
        :param id:
        :return:
        """

        if 'provider' not in request.user.userprofile.get_current_roles():
            return build_response(request, 403, 'You are not authorized to retrieve digital asset information')

        try:
            asset_manager = AssetManager()
            response = asset_manager.get_provider_asset_info(request.user, asset_id)
        except ObjectDoesNotExist as e:
            return build_response(request, 404, unicode(e))
        except PermissionDenied as e:
            return build_response(request, 403, unicode(e))
        except:
            return build_response(request, 500, 'An unexpected error occurred')

        return HttpResponse(json.dumps(response), status=200, mimetype='application/json; charset=utf-8')


class UploadCollection(Resource):

    @supported_request_mime_types(('application/json', 'multipart/form-data'))
    @authentication_required
    def create(self, request):
        """
        Uploads a new downloadable digital asset
        :param request:
        :return: 201 Created, including the new URL of the asset in the location header
        """

        user = request.user
        profile = user.userprofile
        content_type = get_content_type(request)[0]

        if 'provider' not in profile.get_current_roles() and not user.is_staff:
            return build_response(request, 403, "You don't have the seller role")

        asset_manager = AssetManager()
        try:
            if content_type == 'application/json':
                data = json.loads(request.body)
                location = asset_manager.upload_asset(user, data)
            else:
                data = json.loads(request.POST['json'])
                f = request.FILES['file']
                location = asset_manager.upload_asset(user, data, file_=f)

        except ConflictError as e:
            return build_response(request, 409, unicode(e))
        except Exception as e:
            return build_response(request, 400, unicode(e))

        # Fill location header with the URL of the uploaded digital asset
        response = HttpResponse(json.dumps({
            'content': location,
            'contentType': data['contentType']
        }), status=200, mimetype='application/json; charset=utf-8')

        response['Location'] = location
        return response


def _validate_catalog_element(request, element, validator):
    # Validate user permissions
    user = request.user
    if 'provider' not in user.userprofile.get_current_roles() and not user.is_staff:
        return build_response(request, 403, "You don't have the seller role")

    # Parse content
    try:
        data = json.loads(request.body)
    except:
        return build_response(request, 400, 'The content is not a valid JSON document')

    if 'action' not in data:
        return build_response(request, 400, 'Missing required field: action')

    if element not in data:
        return build_response(request, 400, 'Missing required field: product')

    try:
        validator.validate(data['action'], user.userprofile.current_organization, data[element])
    except ValueError as e:
        return build_response(request, 400, unicode(e))
    except ProductError as e:
        return build_response(request, 400, unicode(e))
    except PermissionDenied as e:
        return build_response(request, 403, unicode(e))
    except:
        return build_response(request, 500, 'An unexpected error has occurred')

    return build_response(request, 200, 'OK')


class ValidateCollection(Resource):

    @supported_request_mime_types(('application/json',))
    @authentication_required
    def create(self, request):
        """
        Validates the digital assets contained in a TMForum product Specification
        :param request:
        :return:
        """
        product_validator = ProductValidator()
        return _validate_catalog_element(request, 'product', product_validator)


class ValidateOfferingCollection(Resource):

    @supported_request_mime_types(('application/json',))
    @authentication_required
    def create(self, request):
        """
        Validates the TMForum product offering selling a product specification
        :param request:
        :return:
        """
        offering_validator = OfferingValidator()
        return _validate_catalog_element(request, 'offering', offering_validator)
