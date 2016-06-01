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

from decimal import Decimal


class PriceResolver:

    _applied_sdrs = None

    def __init__(self):
        self._applied_sdrs = []
        self._alteration_applied = False

    def _pay_per_use_preprocesing(self, use_models, accounting_info):
        """
           Process pay-per-use payments and call the corresponding
           price calculator
       """

        price = Decimal('0')
        duty_free = Decimal('0')

        for component in use_models:
            related_accounting = []

            # Get the related accounting info
            partial_price = Decimal('0')
            partial_duty_free = Decimal('0')

            for sdr in accounting_info:
                if sdr['unit'].lower() == component['unit'].lower():
                    sdr_info = {
                        'usage_id': sdr['usage_id'],
                        'value': sdr['value']
                    }
                    comp_price = (Decimal(sdr['value']) * Decimal(component['value']))
                    partial_price += comp_price
                    sdr_info['price'] = unicode(comp_price)

                    comp_duty_free = (Decimal(sdr['value']) * Decimal(component['duty_free']))
                    partial_duty_free += comp_duty_free
                    sdr_info['duty_free'] = unicode(comp_duty_free)

                    # Save the information of the SDR document which is needed for further precessing
                    related_accounting.append(sdr_info)

            # Include the applied SDRs
            self._applied_sdrs.append({
                'model': component,
                'accounting': related_accounting,
                'price': unicode(partial_price),
                'duty_free': unicode(partial_duty_free)
            })

            price += partial_price
            duty_free += partial_duty_free

        return price, duty_free

    def is_altered(self):
        return self._alteration_applied

    def _process_alteration(self, alteration, price, duty_free):
        # Check if there is a condition
        partial_price, partial_duty = Decimal(0), Decimal(0)
        self._alteration_applied = True

        if 'condition' in alteration:
            condition_handlers = {
                'eq': price.__eq__,
                'lt': price.__lt__,
                'gt': price.__gt__,
                'le': price.__le__,
                'ge': price.__ge__,
            }
            op = alteration['condition']['operation']
            value = alteration['condition']['value']

            self._alteration_applied = condition_handlers[op](Decimal(value))

        if self._alteration_applied:
            # Check if the alteration is a percentage or a fixed value
            if isinstance(alteration['value'], dict):
                partial_price = Decimal(alteration['value']['value'])
                partial_duty = Decimal(alteration['value']['duty_free'])
            else:
                partial_price = (Decimal(alteration['value']) * price) / Decimal('100')
                partial_duty = (Decimal(alteration['value']) * duty_free) / Decimal('100')

            # Check if the alteration is a discount
            if alteration['type'] == 'discount':
                partial_price *= Decimal('-1')
                partial_duty *= Decimal('-1')

        return partial_price, partial_duty

    def get_applied_sdr(self):
        """
           Returns the applied sdrs in a pay-per-use
           charging
       """
        return self._applied_sdrs

    def resolve_price(self, pricing_model, accounting_info=None):
        """
           Calculates a price to be charged using a pricing
           model and accounting info.
       """

        price = Decimal('0')
        duty_free = Decimal('0')
        # Check the pricing model
        if 'single_payment' in pricing_model:
            for payment in pricing_model['single_payment']:
                price += Decimal(payment['value'])
                duty_free += Decimal(payment['duty_free'])

        if 'subscription' in pricing_model:
            for payment in pricing_model['subscription']:
                price += Decimal(payment['value'])
                duty_free += Decimal(payment['duty_free'])

        if 'pay_per_use' in pricing_model:
            # Calculate the payment associated with the price component
            partial_price, partial_duty_free = self._pay_per_use_preprocesing(
                pricing_model['pay_per_use'], accounting_info)

            price += partial_price
            duty_free += partial_duty_free

        # Apply price alterations if existing
        if 'alteration' in pricing_model:
            partial_price, partial_duty_free = self._process_alteration(pricing_model['alteration'], price, duty_free)

            price += partial_price
            duty_free += partial_duty_free

        # If the price is negative i.e too much deductions
        # the value is set to 0
        if price < Decimal('0'):
            price = Decimal('0')

        # The result must contain two decimal places
        price = price.quantize(Decimal('10') ** -2)
        duty_free = duty_free.quantize(Decimal('10') ** -2)

        return unicode(price), unicode(duty_free)
