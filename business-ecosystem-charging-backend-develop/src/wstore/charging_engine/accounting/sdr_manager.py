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

from datetime import datetime

from django.core.exceptions import PermissionDenied
from django.contrib.auth.models import User

from wstore.models import Organization
from wstore.ordering.models import Order


class SDRManager(object):

    def _get_order_contract(self, order_id, product_id):
        # Get the order
        order = None
        contract = None

        try:
            order = Order.objects.get(order_id=order_id)
        except:
            pass

        try:
            contract = order.get_product_contract(product_id)
        except:
            pass

        return order, contract

    def _get_datetime(self, time):
        try:
            time_stamp = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S.%f')
        except:
            time_stamp = datetime.strptime(time, '%Y-%m-%d %H:%M:%S.%f')

        return time_stamp

    def get_sdr_values(self, sdr):
        expected_fields = ['orderid', 'productid', 'correlationnumber', 'unit', 'value']
        values = {}

        if 'usageCharacteristic' not in sdr:
            raise ValueError('Missing required field usageCharacteristic')

        for usage_value in sdr['usageCharacteristic']:
            if usage_value['name'].lower() in expected_fields:
                if usage_value['name'].lower() not in values:
                    values[usage_value['name'].lower()] = usage_value['value']
                else:
                    raise ValueError('Only a value is supported for characteristic ' + usage_value['name'])

        if len(values) != len(expected_fields):
            raise ValueError('Missing mandatory characteristics, must be: orderId, productId, correlationNumber, unit, value')

        return values

    def validate_sdr(self, sdr):
        if sdr['status'].lower() != 'received':
            raise ValueError('Invalid initial status, must be Received')

        sdr_values = self.get_sdr_values(sdr)
        order, contract = self._get_order_contract(sdr_values['orderid'], sdr_values['productid'])

        if order is None:
            raise ValueError('Invalid orderId, the order does not exists')

        if contract is None:
            raise ValueError('Invalid productId, the contract does not exist')

        # Check that the value field is a valid number
        try:
            float(sdr_values['value'])
        except:
            raise ValueError('The provided value is not a valid number')

        if 'relatedParty' not in sdr:
            raise ValueError('Missing required field relatedParty')

        # Check that the customer exist
        customer_name = sdr['relatedParty']['id']
        customer = Organization.objects.filter(name=customer_name)

        if not len(customer):
            raise ValueError('The specified customer ' + customer_name + ' does not exist')

        # Check if the user making the request belongs to the customer organization
        user = User.objects.get(username=customer_name)

        for org in user.userprofile.organizations:
            if org['organization'] == order.owner_organization.pk:
                break
        else:
            raise PermissionDenied("You don't belong to the customer organization")

        # Validate that the price mode included in the contract correspond to the one specified in the SDR
        price_model = contract.pricing_model
        if 'pay_per_use' not in price_model:
            raise ValueError('The pricing model of the offering does not define pay-per-use components')

        # Check the correlation number and timestamp
        if int(sdr_values['correlationnumber']) != contract.correlation_number:
            raise ValueError('Invalid correlation number, expected: ' + unicode(contract.correlation_number))

        # Truncate ms to 3 decimals (database supported)
        sp_time = sdr['date'].split('.')
        milis = sp_time[1]

        if len(milis) > 3:
            milis = milis[:3]

        sdr_time = sp_time[0] + '.' + milis

        time_stamp = self._get_datetime(sdr_time)

        if contract.last_usage is not None and contract.last_usage > time_stamp:
            raise ValueError('The provided timestamp specifies a lower timing than the last SDR received')

        # Check that the pricing model contains the specified unit
        for comp in price_model['pay_per_use']:
            if sdr_values['unit'].lower() == comp['unit'].lower():
                break
        else:
            raise ValueError('The specified unit is not included in the pricing model')

        # Save new usage information
        contract.last_usage = time_stamp
        contract.correlation_number += 1
        order.save()
