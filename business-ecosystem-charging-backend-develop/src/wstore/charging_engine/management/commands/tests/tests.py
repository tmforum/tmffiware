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

from datetime import datetime

from mock import MagicMock, call

from django.test import TestCase

from wstore.charging_engine.management.commands import pending_charges_daemon


class ChargesDaemonTestCase(TestCase):

    tags = ('charges-daemon', )

    def setUp(self):
        # Mock datetime
        pending_charges_daemon.datetime = MagicMock()
        pending_charges_daemon.datetime.now.return_value = datetime(2016, 02, 8)

        # Mock inventory client
        pending_charges_daemon.InventoryClient = MagicMock()

        # Mock notifications handler
        pending_charges_daemon.NotificationsHandler = MagicMock()

        # Mock orders
        pending_charges_daemon.Order = MagicMock()

    def _build_contract(self, pricing, id_):
        contract = MagicMock()
        contract.terminated = False
        contract.pricing_model = pricing
        contract.product_id = id_
        return contract

    def _build_subscription_contract(self, date, id_):
        return self._build_contract({
            'subscription': [{
                'renovation_date': date
            }]
        }, id_)

    def _build_usage_contract(self, date, id_):
        contract = self._build_contract({
            'pay_per_use': []
        }, id_)

        contract.charges = [{
            'concept': 'one time'
        }, {
            'concept': 'use',
            'date': date
        }, {
            'concept': 'one time'
        }]
        return contract

    def _test_charging_daemon(self, contracts):
        # Not subscription
        contract1 = MagicMock()
        contract1.pricing_model = {
            'single_payment': []
        }

        order = MagicMock()
        order.contracts = [contract1] + contracts
        pending_charges_daemon.Order.objects.all.return_value = [order]

        # Execute commands
        command = pending_charges_daemon.Command()
        command.handle()

        # Validate calls
        self.assertEquals([call(), call()], pending_charges_daemon.NotificationsHandler.call_args_list)

        pending_charges_daemon.NotificationsHandler().send_payment_required_notification.assert_called_once_with(order, contracts[2])
        pending_charges_daemon.InventoryClient.assert_called_once_with()
        pending_charges_daemon.InventoryClient().suspend_product.assert_called_once_with('3')

        pending_charges_daemon.NotificationsHandler().send_near_expiration_notification.assert_called_once_with(order, contracts[1], 2)

    def test_subscription_renovation(self):

        # Not expired
        contract1 = self._build_subscription_contract(datetime(2016, 03, 01), '1')

        # About to expire
        contract2 = self._build_subscription_contract(datetime(2016, 02, 10), '2')

        # Expired
        contract3 = self._build_subscription_contract(datetime(2016, 01, 31), '3')

        self._test_charging_daemon([contract1, contract2, contract3])

    def test_usage_renovation(self):
        #import ipdb; ipdb.set_trace()

        # Not expired
        contract1 = self._build_usage_contract(datetime(2016, 03, 01), '1')

        # About to expire
        contract2 = self._build_usage_contract(datetime(2016, 01, 11), '2')

        # Expired
        contract3 = self._build_usage_contract(datetime(2015, 12, 31), '3')

        self._test_charging_daemon([contract1, contract2, contract3])

