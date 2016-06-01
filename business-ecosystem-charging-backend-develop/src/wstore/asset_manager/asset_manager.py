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

import base64
from django.core.exceptions import ObjectDoesNotExist, PermissionDenied
import os
from urlparse import urljoin

from django.conf import settings

from wstore.models import Resource, Context
from wstore.store_commons.rollback import rollback
from wstore.store_commons.utils.name import is_valid_file
from wstore.store_commons.errors import ConflictError


class AssetManager:

    def __init__(self):
        pass

    def _save_resource_file(self, provider, file_):
        # Load file contents
        if isinstance(file_, dict):
            file_name = file_['name']
            content = base64.b64decode(file_['data'])
        else:
            file_name = file_.name
            file_.seek(0)
            content = file_.read()

        # Check file name
        if not is_valid_file(file_name):
            raise ValueError('Invalid file name format: Unsupported character')

        # Create provider dir for assets if it does not exists
        provider_dir = os.path.join(settings.MEDIA_ROOT, 'assets', provider)

        if not os.path.isdir(provider_dir):
            os.mkdir(provider_dir)

        file_path = os.path.join(provider_dir, file_name)
        resource_path = file_path[file_path.index(settings.MEDIA_DIR):]

        if resource_path.startswith('/'):
            resource_path = resource_path[1:]

        # Check if the file already exists
        if os.path.exists(file_path):
            res = Resource.objects.get(resource_path=resource_path)
            if len(res.state):
                # If the resource has state field, it means that a product
                # spec has been created, so it cannot be overridden
                raise ConflictError('The provided digital asset (' + file_name + ') already exists')
            res.delete()

        # Create file
        with open(file_path, "wb") as f:
            f.write(content)

        self.rollback_logger['files'].append(file_path)

        return resource_path

    def _create_resource_model(self, provider, resource_data):
        # Create the resource
        resource = Resource.objects.create(
            provider=provider,
            version=resource_data['version'],
            download_link=resource_data['link'],
            resource_path=resource_data['content_path'],
            content_type=resource_data['content_type'].lower(),
            resource_type=resource_data['resource_type'],
            state=resource_data['state'],
            is_public=resource_data['is_public'],
            meta_info=resource_data['metadata']
        )
        self.rollback_logger['models'].append(resource)

        return resource

    def _load_resource_info(self, provider, data, file_=None):

        # This information will be extracted from the product specification
        resource_data = {
            'content_type': data['contentType'],
            'version': '',
            'resource_type': '',
            'state': '',
            'is_public': data.get('isPublic', False)
        }

        current_organization = provider.userprofile.current_organization

        site = Context.objects.all()[0].site.domain
        if not file_:
            if 'content' not in data:
                raise ValueError('The digital asset file has not been provided')

            resource_data['content_path'] = self._save_resource_file(current_organization.name, data['content'])
        else:
            resource_data['content_path'] = self._save_resource_file(current_organization.name, file_)

        resource_data['link'] = urljoin(site, '/charging/' + resource_data['content_path'])
        resource_data['metadata'] = data.get('metadata', {})

        return resource_data, current_organization

    @rollback()
    def upload_asset(self, provider, data, file_=None):
        """
        Uploads a new digital asset that will be used to create a product Specification
        :param provider: User uploading the digital asset
        :param data: Information of the asset
        :param file_: Digital asset file, in case it has been provided as multipart/form-data
        :return: The href of the digital asset
        """

        if 'contentType' not in data:
            raise ValueError('Missing required field: contentType')

        resource_data, current_organization = self._load_resource_info(provider, data, file_=file_)
        resource = self._create_resource_model(current_organization, resource_data)

        return resource.get_url()

    def get_resource_info(self, resource):
        return {
            'id': resource.pk,
            'version': resource.version,
            'contentType': resource.content_type,
            'state': resource.state,
            'href': resource.get_uri(),
            'location': resource.get_url(),
            'resourceType': resource.resource_type,
            'metadata': resource.meta_info
        }

    def get_provider_asset_info(self, provider, asset_id):
        try:
            asset = Resource.objects.get(pk=asset_id)
        except:
            raise ObjectDoesNotExist('The specified digital asset does not exists')

        if asset.provider != provider.userprofile.current_organization:
            raise PermissionDenied('You are not authorized to retrieve digital asset information')

        return self.get_resource_info(asset)

    def get_provider_assets_info(self, provider, pagination=None):

        if pagination and ('start' not in pagination or 'limit' not in pagination):
            raise ValueError('Missing required parameter in pagination')

        if pagination and (not int(pagination['start']) > 0 or not int(pagination['limit']) > 0):
            raise ValueError('Invalid pagination limits')

        response = []

        resources = Resource.objects.filter(provider=provider.userprofile.current_organization)

        if pagination:
            x = int(pagination['start']) - 1
            y = x + int(pagination['limit'])

            resources = resources[x:y]

        for res in resources:
            response.append(self.get_resource_info(res))

        return response
