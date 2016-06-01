# -*- coding: utf-8 -*-

# Copyright (c) 2015 CoNWeT Lab., Universidad Politécnica de Madrid

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

EXISTING_INFO = [{
    'pk': '111',
    'version': '1.0',
    'content_type': 'text/plain',
    'state': 'Active',
    'download_link': 'http://localhost/media/resources/resource1',
    'type': 'API',
    'uri': 'http://location/charging/assetManagement/assets/resource1'
}, {
    'pk': '222',
    'version': '2.0',
    'content_type': 'text/plain',
    'state': 'Active',
    'download_link': 'http://localhost/media/resources/resource2',
    'type': 'API',
    'uri': 'http://location/charging/assetManagement/assets/resource2'
}, {
    'pk': '333',
    'version': '2.0',
    'content_type': 'text/plain',
    'state': 'Active',
    'download_link': 'http://localhost/media/resources/resource3',
    'type': 'API',
    'uri': 'http://location/charging/assetManagement/assets/resource3'
}, {
    'pk': '444',
    'version': '1.0',
    'content_type': 'text/plain',
    'state': 'Active',
    'download_link': 'http://localhost/media/resources/resource4',
    'type': 'API',
    'uri': 'http://location/charging/assetManagement/assets/resource4'
}]

RESOURCE_DATA1 = {
    'id': '111',
    'version': '1.0',
    'contentType': 'text/plain',
    'state': 'Active',
    'location': 'http://localhost/media/resources/resource1',
    'href': 'http://location/charging/assetManagement/assets/resource1',
    'resourceType': 'API',
    'metadata': {}
}

RESOURCE_DATA2 = {
    'id': '222',
    'version': '2.0',
    'contentType': 'text/plain',
    'state': 'Active',
    'location': 'http://localhost/media/resources/resource2',
    'href': 'http://location/charging/assetManagement/assets/resource2',
    'resourceType': 'API',
    'metadata': {}
}

RESOURCE_DATA3 = {
    'id': '333',
    'version': '2.0',
    'contentType': 'text/plain',
    'state': 'Active',
    'location': 'http://localhost/media/resources/resource3',
    'href': 'http://location/charging/assetManagement/assets/resource3',
    'resourceType': 'API',
    'metadata': {}
}

RESOURCE_DATA4 = {
    'id': '444',
    'version': '1.0',
    'contentType': 'text/plain',
    'state': 'Active',
    'location': 'http://localhost/media/resources/resource4',
    'href': 'http://location/charging/assetManagement/assets/resource4',
    'resourceType': 'API',
    'metadata': {}
}

UPLOAD_CONTENT = {
    'contentType': 'application/x-widget',
    'content': {
        'name': 'example.wgt',
        'data': 'VGVzdCBkYXRhIGNvbnRlbnQ='
    }
}

UPLOAD_INV_FILENAME = {
    'contentType': 'application/x-widget',
    'content': {
        'name': 'exampleÑ.wgt',
        'data': 'VGVzdCBkYXRhIGNvbnRlbnQ='
    }
}

MISSING_TYPE = {
    'content': {
        'name': 'exampleÑ.wgt',
        'data': 'VGVzdCBkYXRhIGNvbnRlbnQ='
    }
}