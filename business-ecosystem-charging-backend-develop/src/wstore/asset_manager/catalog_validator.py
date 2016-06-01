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

from wstore.asset_manager.errors import ProductError


class CatalogValidator:

    def __init__(self):
        pass

    def _get_characteristic_value(self, characteristic):
        if len(characteristic['productSpecCharacteristicValue']) > 1:
            raise ProductError('The characteristic ' + characteristic['name'] + ' must not contain multiple values')

        return characteristic['productSpecCharacteristicValue'][0]['value']

    def parse_characteristics(self, product_spec):
        expected_chars = {
            'asset type': [],
            'media type': [],
            'location': []
        }
        asset_type = None
        media_type = None
        location = None

        if 'productSpecCharacteristic' in product_spec:

            # Extract the needed characteristics for processing digital assets
            is_digital = False
            for char in product_spec['productSpecCharacteristic']:
                if char['name'].lower() in expected_chars:
                    is_digital = True
                    expected_chars[char['name'].lower()].append(self._get_characteristic_value(char))

            for char_name in expected_chars:
                # Validate the existance of the characteristic
                if not len(expected_chars[char_name]) and is_digital:
                    raise ProductError('Digital product specifications must contain a ' + char_name + ' characteristic')

                # Validate that only a value has been provided
                if len(expected_chars[char_name]) > 1:
                    raise ProductError('The product specification must not contain more than one ' + char_name + ' characteristic')

            if is_digital:
                asset_type = expected_chars['asset type'][0]
                media_type = expected_chars['media type'][0]
                location = expected_chars['location'][0]

        return asset_type, media_type, location

    def validate_creation(self, provider, catalog_element):
        pass

    def validate_update(self, provider, catalog_element):
        pass

    def validate_upgrade(self, provider, catalog_element):
        pass

    def validate_deletion(self, provider, catalog_element):
        pass

    def validate(self, action, provider, catalog_element):
        validators = {
            'create': self.validate_creation,
            'update': self.validate_update,
            'upgrade': self.validate_upgrade,
            'delete': self.validate_deletion
        }

        if action not in validators:
            msg = 'The provided action (' + action
            msg += ') is not valid. Allowed values are create, update, upgrade, and delete'
            raise ValueError(msg)

        validators[action](provider, catalog_element)
