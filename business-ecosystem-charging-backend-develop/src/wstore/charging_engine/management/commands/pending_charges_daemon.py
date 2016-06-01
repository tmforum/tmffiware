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

from datetime import datetime, timedelta

from django.core.management.base import BaseCommand

from wstore.ordering.models import Order
from wstore.admin.users.notification_handler import NotificationsHandler
from wstore.ordering.inventory_client import InventoryClient


class Command(BaseCommand):

    def _check_renovation_date(self, renovation_date, order, contract):
        now = datetime.now()

        timed = renovation_date - now

        if timed.days < 7:
            handler = NotificationsHandler()

            if timed.days < 0:
                # Notify that the subscription has finished
                handler.send_payment_required_notification(order, contract)

                # Set the product as suspended
                client = InventoryClient()
                client.suspend_product(contract.product_id)
            else:
                # There is less than a week remaining
                handler.send_near_expiration_notification(order, contract, timed.days)

    def _process_subscription_item(self, order, contract, item):
        try:
            self._check_renovation_date(item['renovation_date'], order, contract)
        except:
            pass

    def _process_usage_item(self, order, contract):
        try:
            # Search last usage charge
            last_charge = None
            for charge in reversed(contract.charges):
                if charge['concept'] == 'use':
                    last_charge = charge['date']
                    break

            # No use charge has been applied yet
            if last_charge is None:
                last_charge = order.date

            # Usage payments are renovated every 30 days
            self._check_renovation_date(last_charge + timedelta(days=30), order, contract)
        except:
            pass

    def handle(self, *args, **options):
        """
        Periodic task in charge of checking recurring and usage payments dates
        in order to notify customers and suspend services if needed
        :return:
        """

        # Check contracts
        for order in Order.objects.all():
            for contract in order.contracts:
                if 'pay_per_use' in contract.pricing_model and not contract.terminated:
                        self._process_usage_item(order, contract)

                if 'subscription' in contract.pricing_model and not contract.terminated:
                    # Validate renovation date
                    for item in contract.pricing_model['subscription']:
                        self._process_subscription_item(order, contract, item)
