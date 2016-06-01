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

from __future__ import absolute_import
from __future__ import unicode_literals

import threading
import importlib
from bson import ObjectId
from datetime import datetime, timedelta

from django.conf import settings
from wstore.charging_engine.accounting.sdr_manager import SDRManager
from wstore.charging_engine.accounting.usage_client import UsageClient

from wstore.charging_engine.price_resolver import PriceResolver
from wstore.charging_engine.charging.cdr_manager import CDRManager
from wstore.charging_engine.invoice_builder import InvoiceBuilder
from wstore.ordering.errors import OrderingError
from wstore.ordering.models import Order
from wstore.ordering.ordering_client import OrderingClient
from wstore.store_commons.database import get_database_connection
from wstore.admin.users.notification_handler import NotificationsHandler
from wstore.store_commons.utils.units import recurring_periods


class ChargingEngine:

    def __init__(self, order):
        self._order = order
        self._price_resolver = PriceResolver()
        self.charging_processors = {
            'initial': self._process_initial_charge,
            'renovation': self._process_renovation_charge,
            'use': self._process_use_charge
        }
        self.end_processors = {
            'initial': self._end_initial_charge,
            'renovation': self._end_renovation_charge,
            'use': self._end_use_charge
        }

    def _initial_charge_timeout(self, order):
        ordering_client = OrderingClient()
        raw_order = ordering_client.get_order(order.order_id)

        # Setting all the items as Failed, set the whole order as failed
        # ordering_client.update_state(raw_order, 'Failed')
        ordering_client.update_items_state(raw_order, 'Failed')

        order.delete()

    def _renew_charge_timeout(self, order):
        order.state = 'paid'
        order.pending_payment = {}

        order.save()

    def _timeout_handler(self):

        db = get_database_connection()

        # Uses an atomic operation to get and set the _lock value in the purchase
        # document
        pre_value = db.wstore_order.find_one_and_update(
            {'_id': ObjectId(self._order.pk)},
            {'$set': {'_lock': True}}
        )

        # If _lock not exists or is set to false means that this function has
        # acquired the resource
        if '_lock' not in pre_value or not pre_value['_lock']:

            # Only rollback if the state is pending
            if pre_value['state'] == 'pending':
                order = Order.objects.get(pk=self._order.pk)
                timeout_processors = {
                    'initial': self._initial_charge_timeout,
                    'renovation': self._renew_charge_timeout,
                    'use': self._renew_charge_timeout
                }
                timeout_processors[self._concept](order)

            db.wstore_order.find_one_and_update(
                {'_id': ObjectId(self._order.pk)},
                {'$set': {'_lock': False}}
            )

    def _charge_client(self, transactions):

        # Load payment client
        cln_str = settings.PAYMENT_CLIENT
        client_package, client_class = cln_str.rsplit('.', 1)

        payment_client = getattr(importlib.import_module(client_package), client_class)

        # build the payment client
        client = payment_client(self._order)

        client.start_redirection_payment(transactions)
        checkout_url = client.get_checkout_url()

        # Set timeout for PayPal transaction to 5 minutes
        t = threading.Timer(300, self._timeout_handler)
        t.start()

        return checkout_url

    def _calculate_renovation_date(self, unit):
        return datetime.now() + timedelta(days=recurring_periods[unit.lower()])

    def _end_initial_charge(self, contract, transaction):
        # If a subscription part has been charged update renovation date
        related_model = transaction['related_model']
        if 'subscription' in related_model:
            updated_subscriptions = []

            for subs in contract.pricing_model['subscription']:
                up_sub = subs
                # Calculate renovation date
                up_sub['renovation_date'] = self._calculate_renovation_date(subs['unit'])
                updated_subscriptions.append(up_sub)

            contract.pricing_model['subscription'] = updated_subscriptions
            related_model['subscription'] = updated_subscriptions

        # Save offerings in org profile
        self._order.owner_organization.acquired_offerings.append(contract.offering.pk)
        self._order.owner_organization.save()

    def _end_renovation_charge(self, contract, transaction):

        related_model = transaction['related_model']
        # Process contract subscriptions
        for subs in related_model['subscription']:
            subs['renovation_date'] = self._calculate_renovation_date(subs['unit'])
            updated_subscriptions = related_model['subscription']

            if 'unmodified' in related_model:
                updated_subscriptions.extend(related_model['unmodified'])

            # Save pricing model with new renovation dates
            contract.pricing_model['subscription'] = updated_subscriptions
            related_model['subscription'] = updated_subscriptions

    def _end_use_charge(self, contract, transaction):
        # Change applied usage documents SDR Guided to Rated
        usage_client = UsageClient()
        for sdr_info in transaction['applied_accounting']:
            for sdr in sdr_info['accounting']:

                usage_client.rate_usage(
                    sdr['usage_id'],
                    unicode(contract.last_charge),
                    sdr['duty_free'],
                    sdr['price'],
                    sdr_info['model']['tax_rate'],
                    transaction['currency'],
                    contract.product_id
                )

        transaction['related_model']['accounting'] = transaction['applied_accounting']

    def end_charging(self, transactions, concept):
        """
        Process the second step of a payment once the customer has approved the charge
        :param transactions: List of transactions applied including the total price and the related model
        :param concept: Concept of the charge, it can be initial, renovation, or use
        """

        # Update purchase state
        if self._order.state == 'pending':
            self._order.state = 'paid'
            self._order.save()

        time_stamp = datetime.now()

        self._order.pending_payment = {}

        for transaction in transactions:
            contract = self._order.get_item_contract(transaction['item'])
            # Update contracts
            contract.charges.append({
                'date': time_stamp,
                'cost': transaction['price'],
                'currency': transaction['currency'],
                'concept': concept
            })

            contract.last_charge = time_stamp

            self.end_processors[concept](contract, transaction)

            # If the customer has been charged create the CDR
            cdr_manager = CDRManager(self._order, contract)
            cdr_manager.generate_cdr(transaction['related_model'], unicode(time_stamp))

        self._order.save()

        # TODO: Improve the rollback in case of unexpected exception
        try:
            # Generate the invoice
            invoice_builder = InvoiceBuilder(self._order)
            invoice_builder.generate_invoice(transactions, concept)

            # Send notifications if required
            handler = NotificationsHandler()
            if concept == 'initial':
                # Send customer and provider notifications
                handler.send_acquired_notification(self._order)
                for cont in self._order.contracts:
                    handler.send_provider_notification(self._order, cont)

            elif concept == 'renovation' or concept == 'use':
                handler.send_renovation_notification(self._order, transactions)
        except:
            pass

    def _save_pending_charge(self, transactions):
        pending_payment = {
            'transactions': transactions,
            'concept': self._concept
        }

        self._order.pending_payment = pending_payment
        self._order.save()

    def _append_transaction(self, transactions, contract, related_model, accounting=None):
        # Call the price resolver
        price, duty_free = self._price_resolver.resolve_price(related_model, accounting)

        if 'alteration' in related_model and not self._price_resolver.is_altered():
            del related_model['alteration']

        transaction = {
            'price': price,
            'duty_free': duty_free,
            'description': contract.offering.description,
            'currency': contract.pricing_model['general_currency'],
            'related_model': related_model,
            'item': contract.item_id
        }

        # Get the applied accounting info is needed
        if accounting is not None:
            transaction['applied_accounting'] = self._price_resolver.get_applied_sdr()

        transactions.append(transaction)

    def _process_initial_charge(self, contracts):
        """
        Resolves initial charges, which can include single payments or the initial payment of a subscription
        :return: The URL where redirecting the customer to approve the charge
        """

        transactions = []
        redirect_url = None

        for contract in contracts:
            related_model = {}
            # Check if there are price parts different from pay per use
            if 'single_payment' in contract.pricing_model:
                related_model['single_payment'] = contract.pricing_model['single_payment']

            if 'subscription' in contract.pricing_model:
                related_model['subscription'] = contract.pricing_model['subscription']

            if 'alteration' in contract.pricing_model:
                related_model['alteration'] = contract.pricing_model['alteration']

            if len(related_model):
                self._append_transaction(transactions, contract, related_model)

        if len(transactions):
            # Make the charge
            redirect_url = self._charge_client(transactions)
            self._save_pending_charge(transactions)
        else:
            # If it is not necessary to charge the customer, the state is set to paid
            self._order.state = 'paid'
            self.end_charging(transactions, self._concept)

        return redirect_url

    def _execute_renovation_transactions(self, transactions, err_msg):
        if len(transactions):
            # Make the charge
            redirect_url = self._charge_client(transactions)
            self._save_pending_charge(transactions)
        else:
            # If it is not necessary to charge the customer, the state is set to paid
            self._order.state = 'paid'
            self._order.save()
            raise OrderingError(err_msg)

        return redirect_url

    def _process_renovation_charge(self, contracts):
        """
        Resolves renovation charges, which includes the renovation of subscriptions and optionally usage payments
        :return: The URL where redirecting the customer to approve the charge
        """

        self._order.state = 'pending'

        now = datetime.now()
        transactions = []
        for contract in contracts:
            # Check if the contract has any recurring model
            if 'subscription' not in contract.pricing_model:
                continue

            # Determine the price parts to renovate
            related_model = {
                'subscription': []
            }

            unmodified = []
            for s in contract.pricing_model['subscription']:
                renovation_date = s['renovation_date']
                if renovation_date < now:
                    related_model['subscription'].append(s)
                else:
                    unmodified.append(s)

            # Save unmodified recurring payment (not ended yed)
            if len(unmodified):
                related_model['unmodified'] = unmodified

            # Calculate the price to be charged if required
            if len(related_model['subscription']):
                self._append_transaction(transactions, contract, related_model)

        return self._execute_renovation_transactions(transactions, 'There is not recurring payments to renovate')

    def _parse_raw_accounting(self, usage):
        sdr_manager = SDRManager(self._order.customer)
        sdrs = []

        for usage_document in usage:
            sdr_values = sdr_manager.get_sdr_values(usage_document)
            sdr_values.update({'usage_id': usage_document['id']})
            sdrs.append(sdr_values)

        return sdrs

    def _process_use_charge(self, contracts):
        """
        Resolves usage charges, which includes pay-per-use payments
        :return: The URL where redirecting the customer to approve the charge
        """
        self._order.state = 'pending'

        transactions = []
        usage_client = UsageClient()
        for contract in contracts:
            if 'pay_per_use' not in contract.pricing_model:
                continue

            related_model = {
                'pay_per_use': contract.pricing_model['pay_per_use']
            }

            accounting = self._parse_raw_accounting(usage_client.get_customer_usage(
                    self._order.owner_organization.name, contract.product_id, state='Guided'))

            if len(accounting) > 0:
                self._append_transaction(
                    transactions,
                    contract,
                    related_model,
                    accounting=accounting
                )

        return self._execute_renovation_transactions(transactions, 'There is not usage payments to renovate')

    def resolve_charging(self, type_='initial', related_contracts=None):
        """
        Calculates the charge of a customer depending on the pricing model and the type of charge.
        :param type_: Type of charge, it defines if it is an initial charge, a renovation or a usage based charge
        :param related_contracts: optional field that can be used to specify a set of contracts to be processed.
        If None all the contracts in the order are processed
        :return: The URL where redirecting the user to be charged (PayPal)
        """

        self._concept = type_

        if type_ not in self.charging_processors:
            raise ValueError('Invalid charge type, must be initial, renovation, or use')

        if related_contracts is None:
            related_contracts = self._order.contracts

        return self.charging_processors[type_](related_contracts)
