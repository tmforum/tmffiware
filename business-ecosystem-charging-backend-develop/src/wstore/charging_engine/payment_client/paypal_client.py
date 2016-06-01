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
from decimal import Decimal

import paypalrestsdk

from wstore.charging_engine.payment_client.payment_client import PaymentClient
from wstore.ordering.errors import PaymentError
from wstore.models import Context

# Paypal creadetials
PAYPAL_CLIENT_ID = 'AVOLRuc4jN599UD5FMLHv07T7pnmh76zrllx60cQ-fPK39Bu4yR2iOCzNrzqou6XmAFbnuYhdMY4cExY'
PAYPAL_CLIENT_SECRET = 'EAgeaOxAgJ5ZMMu9Tf4riICdT7Sz2y77PRBwUIYppNlf_xw2Q0WD1_jCG4YzSLNxQFevkNnFovtT02u7'
MODE = 'sandbox'  # sandbox or live


class PayPalClient(PaymentClient):

    _purchase = None
    _checkout_url = None

    def __init__(self, order):
        self._order = order
        # Configure API connection
        paypalrestsdk.configure({
            "mode": MODE,
            "client_id": PAYPAL_CLIENT_ID,
            "client_secret": PAYPAL_CLIENT_SECRET
        })

    def start_redirection_payment(self, transactions):

        # Build URL
        url = Context.objects.all()[0].site.domain
        if url[-1] != '/':
            url += '/'

        # Build payment object
        payment = paypalrestsdk.Payment({
            'intent': 'sale',
            'payer': {
                'payment_method': 'paypal'
            },
            'redirect_urls': {
                'return_url': url + 'payment?action=accept&ref=' + self._order.pk,
                'cancel_url': url + 'payment?action=cancel&ref=' + self._order.pk
            },
            'transactions': [{
                'amount': {
                    'total': unicode(t['price']),
                    'currency': t['currency']
                },
                'description': t['description']
            } for t in transactions]
        })

        # Create Payment
        if not payment.create():

            # Check if the error is due to a problem supporting multiple transactions
            details = payment.error['details']
            if len(transactions) > 1 and len(details) == 1 and \
                    details[0]['issue'] == 'Only single payment transaction currently supported':

                # Aggregate transactions in a single payment if possible
                current_curr = transactions[0]['currency']
                total = Decimal('0')
                items = ''

                for t in transactions:
                    # Only if all the transactions have the same currency they can be aggregated
                    if t['currency'] != current_curr:
                        break

                    total += Decimal(t['price'])
                    items += t['item'] + ':' + t['price'] + '<' + t['description'] + '>'
                else:
                    msg = 'All your order items have been aggregated, since PayPal is not able '
                    msg += 'to process multiple transactions in this moment.                   '
                    msg += 'Order composed of the following items ' + items

                    self.start_redirection_payment([{
                        'price': unicode(total),
                        'currency': current_curr,
                        'description': msg
                    }])
                    return

            raise PaymentError("The payment cannot be created: " + payment.error.message)

        # Extract URL where redirecting the customer
        response = payment.to_dict()
        for l in response['links']:
            if l['rel'] == 'approval_url':
                self._checkout_url = l['href']
                break

    def direct_payment(self, currency, price, credit_card):
        pass

    def end_redirection_payment(self, token, payer_id):
        payment = paypalrestsdk.Payment.find(token)

        if not payment.execute({"payer_id": payer_id}):
            raise PaymentError("The payment cannot be executed: " + payment.error)

        sales_ids = []
        response = payment.to_dict()
        for t in response['transactions']:
            for r in t['related_resources']:
                sales_ids.append(r['sale']['id'])

        return sales_ids

    def refund(self, sale_id):
        sale = paypalrestsdk.Sale.find(sale_id)

        if not sale.refund({}):
            raise PaymentError("The refund cannot be completed: " + sale.error)

    def get_checkout_url(self):
        return self._checkout_url
