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

import re
import requests
from decimal import Decimal
from datetime import datetime

from wstore.models import Organization, Resource
from wstore.ordering.inventory_client import InventoryClient
from wstore.store_commons.rollback import rollback
from wstore.charging_engine.charging_engine import ChargingEngine
from wstore.ordering.errors import OrderingError
from wstore.ordering.models import Order, Contract, Offering
from wstore.asset_manager.product_validator import ProductValidator


class OrderingManager:

    def __init__(self):
        self._customer = None
        self._validator = ProductValidator()

    def _download(self, url, element, item_id):
        r = requests.get(url)

        if r.status_code != 200:
            raise OrderingError('The ' + element + ' specified in order item ' + item_id + ' does not exists')

        return r.json()

    def _download_models(self, item):
        offering_info = self._download(item['productOffering']['href'], 'product offering', item['id'])
        product_info = self._download(offering_info['productSpecification']['href'], 'product specification', item['id'])

        return offering_info, product_info

    def _get_offering(self, item):

        # Download related product offering and product specification
        offering_info, product_info = self._download_models(item)

        # Check if the product is a digital one
        asset_type, media_type, location = self._validator.parse_characteristics(product_info)

        asset = None
        if asset_type is not None and media_type is not None and location is not None:
            asset = Resource.objects.get(download_link=location)

        offering_id = offering_info['id']

        # Check if the offering contains a description
        description = ''
        if 'description' in offering_info:
            description = offering_info['description']

        # Check if the offering has been already loaded in the system
        if len(Offering.objects.filter(off_id=offering_id)) > 0:
            offering = Offering.objects.get(off_id=offering_id)

            # If the offering defines a digital product, check if the customer already owns it
            if asset is not None and offering.pk in self._customer.userprofile.current_organization.acquired_offerings:
                raise OrderingError('The customer already owns the digital product offering ' + offering_info['name'] + ' with id ' + offering_id)

            offering.description = description

            offering.version = offering_info['version']
            offering.href = offering_info['href']
            offering.save()
        else:
            # Get offering provider (Owner role)
            for party in product_info['relatedParty']:
                if party['role'].lower() == 'owner':
                    provider = Organization.objects.get(name=party['id'])
                    break
            else:
                raise OrderingError('The product specification included in the order item ' + item['id'] + ' does not contain a valid provider')

            offering = Offering.objects.create(
                off_id=offering_id,
                href=offering_info['href'],
                owner_organization=provider,
                name=offering_info['name'],
                description=description,
                version=offering_info['version'],
                is_digital=asset is not None,
                asset=asset
            )

            self.rollback_logger['models'].append(offering)

        return offering, offering_info

    def _parse_price(self, model_mapper, price):

        if price['priceType'].lower() not in model_mapper:
            raise OrderingError('Invalid price model ' + price['priceType'])

        unit_field = {
            'one time': 'priceType',
            'recurring': 'recurringChargePeriod',
            'usage': 'unitOfMeasure'
        }

        return {
            'value': price['price']['taxIncludedAmount'],
            'unit': price[unit_field[price['priceType'].lower()]].lower(),
            'tax_rate': price['price']['taxRate'],
            'duty_free': price['price']['dutyFreeAmount']
        }

    def _parse_alteration(self, alteration, type_):
        # Alterations cannot specify usage models
        if alteration['priceType'].lower() != 'one time' and alteration['priceType'].lower() != 'recurring':
            raise OrderingError('Invalid priceType in price alteration, it must be one time or recurring')

        # Check if it is a fixed value or a percentage
        if 'percentage' in alteration['price'] and Decimal(alteration['price']['percentage']) > Decimal(0):
            value = alteration['price']['percentage']
        else:
            value = {
                'value': alteration['price']['taxIncludedAmount'],
                'duty_free': alteration['price']['dutyFreeAmount']
            }

        alt_model = {
            'type': type_,
            'value': value,
            'period': alteration['priceType'].lower()
        }

        # Parse condition
        if 'priceCondition' in alteration and len(alteration['priceCondition']):
            exp = re.compile('^(eq|lt|gt|le|ge) \d+(\.\d+)?$')

            if not exp.match(alteration['priceCondition']):
                raise OrderingError('Invalid priceCondition in price alteration, format must be: [eq | lt | gt | le | ge] value')

            op, value = alteration['priceCondition'].split(' ')

            alt_model['condition'] = {
                'operation': op,
                'value': value
            }

        return alt_model

    def _get_effective_pricing(self, item_id, product_price, offering_info):
        # Search the pricing chosen by the user
        matches = 0
        price = None
        for off_price in offering_info['productOfferingPrice']:

            # Change the price to string in order to avoid problems with floats
            product_price['price']['amount'] = unicode(product_price['price']['amount'])

            # Validate that all pricing fields matches
            if off_price['priceType'] == product_price['priceType'] and \
                (('unitOfMeasure' not in off_price and 'unitOfMeasure' not in product_price) or
                  ('unitOfMeasure' in off_price and 'unitOfMeasure' in product_price and off_price['unitOfMeasure'] == product_price['unitOfMeasure'])) and \
                    (('recurringChargePeriod' not in off_price and 'recurringChargePeriod' not in product_price) or
                      ('recurringChargePeriod' in off_price and 'recurringChargePeriod' in product_price and off_price['recurringChargePeriod'] == product_price['recurringChargePeriod'])) and \
                        Decimal(off_price['price']['taxIncludedAmount']) == Decimal(product_price['price']['amount']) and \
                          off_price['price']['currencyCode'] == product_price['price']['currency']:

                matches += 1
                price = off_price

        if not matches:
            raise OrderingError('The product price included in orderItem ' + item_id + ' does not match with any of the prices included in the related offering')
        elif matches > 1:
            raise OrderingError('The product price included in orderItem ' + item_id + ' matches with multiple pricing models of the related offering')

        return price

    def _build_contract(self, item):
        # TODO: Check that the ordering API is actually validating that the chosen pricing and characteristics are valid for the given product

        # Build offering
        offering, offering_info = self._get_offering(item)

        # Build pricing if included
        pricing = {}
        if 'product' in item and 'productPrice' in item['product'] and len(item['product']['productPrice']):

            model_mapper = {
                'one time': 'single_payment',
                'recurring': 'subscription',
                'usage': 'pay_per_use'
            }

            # The productPrice field in the orderItem does not contain all the needed
            # information (neither taxes nor alterations), so extract pricing from the offering
            price = self._get_effective_pricing(item['id'], item['product']['productPrice'][0], offering_info)

            price_unit = self._parse_price(model_mapper, price)

            pricing['general_currency'] = price['price']['currencyCode']
            pricing[model_mapper[price['priceType'].lower()]] = [price_unit]

            # Process price alterations
            if 'productOfferPriceAlteration' in price:
                alteration = price['productOfferPriceAlteration']

                # Check type of alteration (discount or fee)
                if 'discount' in alteration['name'].lower() and 'fee' not in alteration['name'].lower():
                    # Is a discount
                    pricing['alteration'] = self._parse_alteration(alteration, 'discount')

                elif 'discount' not in alteration['name'].lower() and 'fee' in alteration['name'].lower():
                    # Is a fee
                    if 'priceCondition' not in alteration or not len(alteration['priceCondition']):
                        # In this case the alteration is processed as another price
                        price_unit = self._parse_price(model_mapper, alteration)

                        if model_mapper[alteration['priceType'].lower()] not in pricing:
                            pricing[model_mapper[alteration['priceType'].lower()]] = []

                        pricing[model_mapper[alteration['priceType'].lower()]].append(price_unit)
                    else:
                        pricing['alteration'] = self._parse_alteration(alteration, 'fee')
                else:
                    raise OrderingError('Invalid price alteration, it is not possible to determine if it is a discount or a fee')

        # Calculate the revenue sharing class
        revenue_class = offering_info['serviceCandidate']['id']

        return Contract(
            item_id=item['id'],
            pricing_model=pricing,
            revenue_class=revenue_class,
            offering=offering
        )

    def _get_billing_address(self, items):

        def _download_asset(url):
            r = requests.get(url, headers={'Authorization': 'Bearer ' + self._customer.userprofile.access_token })

            if r.status_code != 200:
                raise OrderingError('There was an error at the time of retrieving the Billing Address')

            return r.json()

        billing_account = _download_asset(items[0]['billingAccount'][0]['href'])
        customer_account = _download_asset(billing_account['customerAccount']['href'])
        customer = _download_asset(customer_account['customer']['href'])

        postal_addresses = [contactMedium for contactMedium in customer['contactMedium'] if contactMedium['type'] == 'PostalAddress']

        if len(postal_addresses) != 1:
            raise OrderingError('Provided Billing Account does not contain a Postal Address')

        postal_address = postal_addresses[0]['medium']

        return {
            'street': postal_address['streetOne'] + '\n' + postal_address.get('streetTwo', ''),
            'postal': postal_address['postcode'],
            'city': postal_address['city'],
            'province': postal_address['stateOrProvince'],
            'country': postal_address['country']
        }

    def _process_add_items(self, items, order_id, description):

        new_contracts = []
        for item in items:
            new_contracts.append(self._build_contract(item))

        current_org = self._customer.userprofile.current_organization
        order = Order.objects.create(
            order_id=order_id,
            customer=self._customer,
            owner_organization=current_org,
            date=datetime.now(),
            state='pending',
            tax_address=self._get_billing_address(items),
            contracts=new_contracts,
            description=description
        )

        self.rollback_logger['models'].append(order)

        charging_engine = ChargingEngine(order)
        return charging_engine.resolve_charging()

    def _get_existing_contract(self, inv_client, product_id):
        # Get product info
        raw_product = inv_client.get_product(product_id)

        # Get related order
        order = Order.objects.get(order_id=raw_product['name'].split('=')[1])

        # Get the existing contract
        contract = order.get_product_contract(product_id)

        # TODO: Process pay per use case
        if 'subscription' in contract.pricing_model:
            # Check if there are a pending subscription
            now = datetime.now()

            for subs in contract.pricing_model['subscription']:
                timedelta = subs['renovation_date'] - now
                if timedelta.days > 0:
                    raise OrderingError('You cannot modify a product with a recurring payment until the subscription expires')

        return order, contract

    def _process_modify_items(self, items):
        if len(items) > 1:
            raise OrderingError('Only a modify item is supported per order item')

        item = items[0]
        if 'product' not in item:
            raise OrderingError('It is required to specify product information in modify order items')

        product = item['product']

        if 'id' not in product:
            raise OrderingError('It is required to provide product id in modify order items')

        client = InventoryClient()
        order, contract = self._get_existing_contract(client, product['id'])

        # Build the new contract
        new_contract = self._build_contract(item)
        if new_contract.pricing_model != {}:
            contract.pricing_model = new_contract.pricing_model
            contract.revenue_class = new_contract.revenue_class

        order.save()

        # The modified item is treated as an initial payment
        charging_engine = ChargingEngine(order)
        return charging_engine.resolve_charging(type_='initial', related_contracts=[contract])

    def _process_delete_items(self, items):
        for item in items:
            if 'product' not in item:
                raise OrderingError('It is required to specify product information in delete order items')

            product = item['product']

            if 'id' not in product:
                raise OrderingError('It is required to provide product id in delete order items')

            # Set the contract as terminated
            client = InventoryClient()
            order, contract = self._get_existing_contract(client, product['id'])
            contract.terminated = True

            order.save()

            # Terminate product in the inventory
            client.terminate_product(product['id'])


    @rollback()
    def process_order(self, customer, order):
        """
        Process the different order items included in a given ordering depending on its action field
        :param customer:
        :param order:
        :return:
        """

        self._customer = customer

        # Check initial state of the order. It must be Acknowledged
        if order['state'].lower() != 'acknowledged':
            raise OrderingError('Only acknowledged orders can be initially processed')

        # Classify order items by action
        items = {
            'add': [],
            'modify': [],
            'delete': [],
            'no_change': []
        }
        for item in order['orderItem']:
            items[item['action'].lower()].append(item)

        if len(items['add']) and len(items['modify']):
            raise OrderingError('It is not possible to process add and modify items in the same order')

        # Process order items separately depending on its action. no_change items are not processed
        if len(items['delete']):
            self._process_delete_items(items['delete'])

        redirection_url = None
        if len(items['modify']):
            redirection_url = self._process_modify_items(items['modify'])

        # Process add items
        if len(items['add']):

            description = ''
            if 'description' in order:
                description = order['description']

            redirection_url = self._process_add_items(items['add'], order['id'], description)

        return redirection_url
