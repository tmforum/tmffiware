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
import importlib
from bson import ObjectId

from django.conf import settings
from django.core.exceptions import PermissionDenied

from wstore.ordering.inventory_client import InventoryClient
from wstore.ordering.ordering_client import OrderingClient
from wstore.store_commons.resource import Resource
from wstore.store_commons.utils.http import build_response, supported_request_mime_types, authentication_required
from wstore.ordering.models import Order
from wstore.ordering.errors import PaymentError
from wstore.charging_engine.charging_engine import ChargingEngine
from wstore.charging_engine.accounting.sdr_manager import SDRManager
from wstore.store_commons.database import get_database_connection
from wstore.charging_engine.accounting.usage_client import UsageClient


class ServiceRecordCollection(Resource):

    # This method is used to load SDR documents and
    # start the charging process
    @supported_request_mime_types(('application/json',))
    def create(self, request):
        try:
            # Extract SDR document from the HTTP request
            data = json.loads(request.body)
        except:
            # The usage document is not valid, so the state cannot be changed
            return build_response(request, 400, 'The request does not contain a valid JSON object')

        # Validate usage information
        response = None
        try:
            sdr_manager = SDRManager()
            sdr_manager.validate_sdr(data)
        except PermissionDenied as e:
            response = build_response(request, 403, unicode(e))
        except ValueError as e:
            response = build_response(request, 422, unicode(e))
        except:
            response = build_response(request, 500, 'The SDR document could not be processed due to an unexpected error')

        usage_client = UsageClient()
        if response is not None:
            # The usage document is not valid, change its state to Rejected
            usage_client.update_usage_state('Rejected', data)
        else:
            # The usage document is valid, change its state to Guided
            usage_client.update_usage_state('Guided', data)
            response = build_response(request, 200, 'OK')

        # Update usage document state
        return response


class PayPalConfirmation(Resource):

    def _set_initial_states(self, transactions, raw_order, order):
        # Set all order items as in progress
        self.ordering_client.update_state(raw_order, 'InProgress')

        # Set order items of digital products as completed
        involved_items = [t['item'] for t in transactions]

        digital_items = []
        for item in raw_order['orderItem']:
            if item['id'] in involved_items and order.get_item_contract(item['id']).offering.is_digital:
                digital_items.append(item)

        # Oder Items state is not checked
        # self.ordering_client.update_items_state(raw_order, 'InProgress', digital_items)
        self.ordering_client.update_items_state(raw_order, 'Completed', digital_items)

    def _set_renovation_states(self, transactions, raw_order, order):
        inventory_client = InventoryClient()

        for transaction in transactions:
            try:
                contract = order.get_item_contract(transaction['item'])
                inventory_client.activate_product(contract.product_id)
            except:
                pass

    # This method is used to receive the PayPal confirmation
    # when the customer is paying using his PayPal account
    @supported_request_mime_types(('application/json',))
    @authentication_required
    def create(self, request):

        order = None
        concept = None
        self.ordering_client = OrderingClient()
        try:
            # Extract payment information
            data = json.loads(request.body)

            if 'reference' not in data or 'paymentId' not in data or 'payerId' not in data:
                raise ValueError('Missing required field. It must contain reference, paymentId, and payerId')

            reference = data['reference']
            token = data['paymentId']
            payer_id = data['payerId']

            if not Order.objects.filter(pk=reference):
                raise ValueError('The provided reference does not identify a valid order')

            db = get_database_connection()

            # Uses an atomic operation to get and set the _lock value in the purchase
            # document
            pre_value = db.wstore_order.find_one_and_update(
                {'_id': ObjectId(reference)},
                {'$set': {'_lock': True}}
            )

            # If the value of _lock before setting it to true was true, means
            # that the time out function has acquired it previously so the
            # view ends
            if not pre_value or '_lock' in pre_value and pre_value['_lock']:
                raise PaymentError('The timeout set to process the payment has finished')

            order = Order.objects.get(pk=reference)
            raw_order = self.ordering_client.get_order(order.order_id)
            pending_info = order.pending_payment
            concept = pending_info['concept']

            # If the order state value is different from pending means that
            # the timeout function has completely ended before acquiring the resource
            # so _lock is set to false and the view ends
            if pre_value['state'] != 'pending':
                db.wstore_order.find_one_and_update(
                    {'_id': ObjectId(reference)},
                    {'$set': {'_lock': False}}
                )
                raise PaymentError('The timeout set to process the payment has finished')

            # Check that the request user is authorized to end the payment
            if request.user.userprofile.current_organization != order.owner_organization:
                raise PaymentError('You are not authorized to execute the payment')

            transactions = pending_info['transactions']

            # Get the payment client
            # Load payment client
            cln_str = settings.PAYMENT_CLIENT
            client_package, client_class = cln_str.rsplit('.', 1)

            payment_client = getattr(importlib.import_module(client_package), client_class)

            # build the payment client
            client = payment_client(order)
            order.sales_ids = client.end_redirection_payment(token, payer_id)
            order.save()

            charging_engine = ChargingEngine(order)
            charging_engine.end_charging(transactions, concept)

        except Exception as e:

            # Rollback the purchase if existing
            if order is not None and raw_order is not None:
                if concept == 'initial':
                    # Set the order to failed in the ordering API
                    # Set all items as Failed, mark the whole order as failed
                    self.ordering_client.update_items_state(raw_order, 'Failed')
                    order.delete()
                else:
                    order.state = 'paid'
                    order.pending_payment = {}
                    order.save()

            expl = ' due to an unexpected error'
            err_code = 500
            if isinstance(e, PaymentError) or isinstance(e, ValueError):
                expl = ': ' + unicode(e)
                err_code = 403

            msg = 'The payment has been canceled' + expl
            return build_response(request, err_code, msg)

        # Change states of TMForum resources (orderItems, products, etc)
        # depending on the concept of the payment

        states_processors = {
            'initial': self._set_initial_states,
            'renovation': self._set_renovation_states,
            'use': self._set_renovation_states
        }
        states_processors[concept](transactions, raw_order, order)

        # _lock is set to false
        db.wstore_order.find_one_and_update(
            {'_id': ObjectId(reference)},
            {'$set': {'_lock': False}}
        )

        return build_response(request, 200, 'Ok')


class PayPalCancellation(Resource):

    # This method is used when the user cancel a charge
    # when is using a PayPal account
    @supported_request_mime_types(('application/json', ))
    @authentication_required
    def create(self, request):
        # In case the user cancels the payment is necessary to update
        # the database in order to avoid an inconsistent state
        try:
            data = json.loads(request.body)
            order = Order.objects.get(pk=data['reference'])

            client = OrderingClient()
            raw_order = client.get_order(order.order_id)

            # Set the order to failed in the ordering API
            # Set all items as Failed, mark the whole order as Failed
            # client.update_state(raw_order, 'Failed')
            client.update_items_state(raw_order, 'Failed')

            order.delete()
        except:
            return build_response(request, 400, 'Invalid request')

        return build_response(request, 200, 'Ok')


class PayPalRefund(Resource):

    # This method is used when the user cancel a charge
    # when is using a PayPal account
    @supported_request_mime_types(('application/json', ))
    @authentication_required
    def create(self, request):
        # In case the user cancels the payment is necessary to update
        # the database in order to avoid an inconsistent state
        try:
            data = json.loads(request.body)
            order = Order.objects.get(order_id=data['orderId'])

            # Get the payment client
            # Load payment client
            cln_str = settings.PAYMENT_CLIENT
            client_package, client_class = cln_str.rsplit('.', 1)

            payment_client = getattr(importlib.import_module(client_package), client_class)

            # build the payment client
            client = payment_client(order)

            for sale in order.sales_ids:
                client.refund(sale)

            order.delete()
        except:
            return build_response(request, 400, 'Sales cannot be refunded')

        return build_response(request, 200, 'Ok')
