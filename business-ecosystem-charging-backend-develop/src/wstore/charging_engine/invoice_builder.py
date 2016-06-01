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

import os
import codecs
import subprocess
from datetime import datetime
from decimal import Decimal

from django.template import loader, Context
from django.conf import settings


class InvoiceBuilder(object):

    def __init__(self, order):
        self._order = order
        self._template_processors = {
            'initial': self._get_initial_parts,
            'renovation': self._get_renovation_parts,
            'use': self._get_use_parts
        }
        self._context_processors = {
            'initial': self._fill_initial_context,
            'renovation': self._fill_renovation_context,
            'use': self._fill_use_context
        }

    def _process_subscription_parts(self, applied_parts, parts, currency):
        if 'subscription' in applied_parts:
            for part in applied_parts['subscription']:
                parts['subs_parts'].append((part['duty_free'], part['tax_rate'], part['value'], currency, part['unit'], str(part['renovation_date'])))

    def _get_initial_parts(self, applied_parts, currency):
        # If initial can only contain single payments and subscriptions
        parts = {
            'single_parts': [],
            'subs_parts': [],
        }
        if 'single_payment' in applied_parts:
            for part in applied_parts['single_payment']:
                parts['single_parts'].append((part['duty_free'], part['tax_rate'], part['value'], currency))

        self._process_subscription_parts(applied_parts, parts, currency)

        # Get the bill template
        bill_template = loader.get_template('contracting/bill_template_initial.html')
        return parts, bill_template

    def _process_usage_component(self, applied_parts, parts, comp_name, part_name, part_sub):
        if comp_name in applied_parts and len(applied_parts[comp_name]) > 0:
            parts[part_name] = []
            parts[part_sub] = 0

            # Fill use tuples for the invoice
            for part in applied_parts[comp_name]:
                model = part['model']
                if 'price_function' in model:
                    unit = 'price function'
                    value_unit = model['text_function']
                    use = '- '
                else:
                    unit = model['unit']
                    value_unit = model['value']

                    # Aggregate use made
                    use = 0
                    for sdr in part['accounting']:
                        use += int(sdr['value'])

                parts[part_name].append((model['label'], unit, value_unit, use, part['price']))
                parts[part_sub] += part['price']

    def _process_usage_parts(self, applied_parts, parts):
        self._process_usage_component(applied_parts, parts, 'charges', 'use_parts', 'use_subtotal')
        self._process_usage_component(applied_parts, parts, 'deductions', 'deduct_parts', 'deduct_subtotal')

    def _get_renovation_parts(self, applied_parts, currency):
        parts = {
            'subs_parts': [],
            'subs_subtotal': 0
        }
        # If renovation, It contains subscriptions
        self._process_subscription_parts(applied_parts, parts, currency)

        # Check use based charges
        self._process_usage_parts(applied_parts, parts)

        # Get the bill template
        bill_template = loader.get_template('contracting/bill_template_renovation.html')
        return parts, bill_template

    def _get_use_parts(self, applied_parts, currency):
        # If use, can only contain pay per use parts or deductions
        parts = {
            'use_parts': [],
            'use_subtotal': 0
        }
        self._process_usage_parts(applied_parts, parts)

        # Get the bill template
        bill_template = loader.get_template('contracting/bill_template_use.html')
        return parts, bill_template

    def _fill_initial_context(self, context, parts):
        context['exists_single'] = False
        context['exists_subs'] = False

        if len(parts['single_parts']) > 0:
            context['single_parts'] = parts['single_parts']
            context['exists_single'] = True

        if len(parts['subs_parts']) > 0:
            context['subs_parts'] = parts['subs_parts']
            context['exists_subs'] = True

    def _fill_renovation_context(self, context, parts):
        context['subs_parts'] = parts['subs_parts']
        context['subs_subtotal'] = parts['subs_subtotal']

        if 'use_parts' in parts:
            context['use'] = True
            context['use_parts'] = parts['use_parts']
            context['use_subtotal'] = parts['use_subtotal']
        else:
            context['use'] = False

        if 'deduct_parts' in parts:
            context['deduction'] = True
            context['deduct_parts'] = parts['deduct_parts']
            context['deduct_subtotal'] = parts['deduct_subtotal']
        else:
            context['deduction'] = False

    def _fill_use_context(self, context, parts):
        context['use_parts'] = parts['use_parts']
        context['use_subtotal'] = parts['use_subtotal']

        if 'deduct_parts' in parts:
            context['deduction'] = True
            context['deduct_parts'] = parts['deduct_parts']
            context['deduct_subtotal'] = parts['deduct_subtotal']
        else:
            context['deduction'] = False

    def generate_invoice(self, transactions, type_):
        """
        Create a PDF invoice based on the price components used to charge the user
        :param transactions: Total amount charged to the customer
        :param type_: Type of the charge, initial, renovation, pay-per-use
        """

        for transaction in transactions:
            currency = transaction['currency']

            # Get invoice context parts and invoice template
            parts, bill_template = self._template_processors[type_](transaction['related_model'], currency)

            tax = self._order.tax_address
            customer_profile = self._order.customer.userprofile

            # Search item contract
            contract = self._order.get_item_contract(transaction['item'])
            offering = contract.offering

            last_charge = contract.last_charge

            if last_charge is None:
                # If last charge is None means that it is the invoice generation
                # associated with a free offering
                date = str(datetime.now()).split(' ')[0]
            else:
                date = str(last_charge).split(' ')[0]

            # Calculate total taxes applied
            tax_value = Decimal(transaction['price']) - Decimal(transaction['duty_free'])

            # Load pricing info into the context
            context = {
                'basedir': settings.BASEDIR,
                'offering_name': offering.name,
                'off_organization': offering.owner_organization.name,
                'off_version': offering.version,
                'ref': self._order.pk,
                'date': date,
                'organization': customer_profile.current_organization.name,
                'customer': customer_profile.complete_name,
                'address': tax.get('street'),
                'postal': tax.get('postal'),
                'city': tax.get('city'),
                'province': tax.get('province'),
                'country': tax.get('country'),
                'subtotal': transaction['duty_free'],
                'tax': unicode(tax_value),
                'total': transaction['price'],
                'cur': currency  # General currency of the invoice
            }

            # Include the corresponding parts in the context
            # depending on the type of applied parts
            self._context_processors[type_](context, parts)

            # Render the invoice template
            bill_code = bill_template.render(Context(context))

            # Create the bill code file
            invoice_name = self._order.pk + '_' + contract.item_id + date
            bill_path = os.path.join(settings.BILL_ROOT, invoice_name + '.html')
            f = codecs.open(bill_path, 'wb', 'utf-8')
            f.write(bill_code)
            f.close()

            in_name = bill_path[:-4] + 'pdf'

            if os.path.exists(in_name):
                in_name = bill_path[:-5] + '_1.pdf'
                invoice_name += '_1'

            # Compile the bill file
            try:
                subprocess.call([settings.BASEDIR + '/create_invoice.sh', bill_path, in_name])
            except:
                raise Exception('Invoice generation problem')

            # Load bill path into the order
            self._order.bills.append(os.path.join(settings.MEDIA_URL, 'bills/' + invoice_name + '.pdf'))
            self._order.save()

        # Remove temporal files
        for file_ in os.listdir(settings.BILL_ROOT):

            if not file_.endswith('.pdf'):
                os.remove(os.path.join(settings.BILL_ROOT, file_))
