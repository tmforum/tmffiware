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

from bson.objectid import ObjectId
from decimal import Decimal
from mock import MagicMock
from nose_parameterized import parameterized

from django.test import TestCase

from wstore.charging_engine.charging import cdr_manager


BASIC_EXP = [{
    'provider': 'provider',
    'correlation': '1',
    'order': '1 3',
    'offering': '4 offering 1.0',
    'product_class': 'one time',
    'description': 'Complete Charging event: 12 EUR',
    'cost_currency': 'EUR',
    'cost_value': '12',
    'tax_value': '2',
    'customer': 'customer',
    'event': 'Charging event',
    'time_stamp': u'2015-10-21 06:13:26.661650'
}]

INITIAL_EXP = [{
    'provider': 'provider',
    'correlation': '1',
    'order': '1 3',
    'offering': '4 offering 1.0',
    'product_class': 'one time',
    'description': 'One time payment: 12 EUR',
    'cost_currency': 'EUR',
    'cost_value': '12',
    'tax_value': '2',
    'customer': 'customer',
    'event': 'One time payment event',
    'time_stamp': u'2015-10-21 06:13:26.661650'
}]

RECURRING_EXP = [{
    'provider': 'provider',
    'correlation': '1',
    'order': '1 3',
    'offering': '4 offering 1.0',
    'product_class': 'one time',
    'description': 'Recurring payment: 12 EUR monthly',
    'cost_currency': 'EUR',
    'cost_value': '12',
    'tax_value': '2',
    'customer': 'customer',
    'event': 'Recurring payment event',
    'time_stamp': u'2015-10-21 06:13:26.661650'
}]

USAGE_EXP = [{
    'provider': 'provider',
    'correlation': '1',
    'order': '1 3',
    'offering': '4 offering 1.0',
    'product_class': 'one time',
    'description': 'Fee per invocation, Consumption: 25',
    'cost_currency': 'EUR',
    'cost_value': '25.0',
    'tax_value': '5.0',
    'customer': 'customer',
    'event': 'Pay per use event',
    'time_stamp': u'2015-10-21 06:13:26.661650'
}]


class CDRGenerationTestCase(TestCase):

    tags = ('cdr', )

    @parameterized.expand([
        ('pricing_provided', {}, Decimal(12), Decimal(10), BASIC_EXP),
        ('initial_charge', {
             'single_payment': [{
                'value': Decimal('12'),
                'unit': 'one time',
                'tax_rate': Decimal('20'),
                'duty_free': Decimal('10')
             }]
         }, None, None, INITIAL_EXP),
        ('recurring_charge', {
             'subscription': [{
                'value': Decimal('12'),
                'unit': 'monthly',
                'tax_rate': Decimal('20'),
                'duty_free': Decimal('10')
             }]
         }, None, None, RECURRING_EXP),
        ('usage', {
            'accounting': [{
                'accounting': [{
                    'order_id': '1',
                    'product_id': '1',
                    'customer': 'customer',
                    'value': '15',
                    'unit': 'invocation'
                }, {
                    'order_id': '1',
                    'product_id': '1',
                    'customer': 'customer',
                    'value': '10',
                    'unit': 'invocation'
                }],
                'model': {
                    'unit': 'invocation',
                    'currency': 'EUR',
                    'value': '1'
                },
                'price': Decimal('25.0'),
                'duty_free': Decimal('20.0')
             }]
        }, None, None, USAGE_EXP)
    ])
    def test_cdr_generation(self, name, applied_parts, price, duty_free, exp_cdrs):
        # Create Mocks
        cdr_manager.RSSAdaptorThread = MagicMock()

        conn = MagicMock()
        cdr_manager.get_database_connection = MagicMock()
        cdr_manager.get_database_connection.return_value = conn

        conn.wstore_organization.find_and_modify.side_effect = [{'correlation_number': 1}, {'correlation_number': 2}]

        order = MagicMock()
        order.order_id = '1'
        order.owner_organization.name = 'customer'

        contract = MagicMock()
        contract.revenue_class = 'one time'
        contract.offering.off_id = '2'
        contract.item_id = '3'
        contract.pricing_model = {
            'general_currency': 'EUR'
        }
        contract.offering.off_id = '4'
        contract.offering.name = 'offering'
        contract.offering.version = '1.0'
        contract.offering.owner_organization.name = 'provider'
        contract.offering.owner_organization.pk = '61004aba5e05acc115f022f0'

        cdr_m = cdr_manager.CDRManager(order, contract)
        cdr_m.generate_cdr(applied_parts, '2015-10-21 06:13:26.661650', price=price, duty_free=duty_free)

        # Validate calls
        conn.wstore_organization.find_and_modify.assert_called_once_with(
            query={'_id': ObjectId('61004aba5e05acc115f022f0')},
            update={'$inc': {'correlation_number': 1}}
        )

        cdr_manager.RSSAdaptorThread.assert_called_once_with(exp_cdrs)
        cdr_manager.RSSAdaptorThread().start.assert_called_once_with()
