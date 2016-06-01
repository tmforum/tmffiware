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

import requests
import threading
from bson import ObjectId

from django.conf import settings

from wstore.store_commons.database import get_database_connection
from wstore.models import Organization


class RSSAdaptorThread(threading.Thread):

    def __init__(self, cdr_info):
        threading.Thread.__init__(self)
        self.cdr = cdr_info

    def run(self):
        r = RSSAdaptor()
        r.send_cdr(self.cdr)


class RSSAdaptor:

    def send_cdr(self, cdr_info):
        # Build CDRs
        data = []
        for cdr in cdr_info:
            time = cdr['time_stamp'].split(' ')
            time = time[0] + 'T' + time[1] + 'Z'

            data.append({
                'cdrSource': settings.WSTOREMAIL,
                'productClass': cdr['product_class'],
                'correlationNumber': cdr['correlation'],
                'timestamp': time,
                'application': cdr['offering'],
                'transactionType': 'C',
                'event': cdr['event'],
                'referenceCode': cdr['order'],
                'description': cdr['description'],
                'chargedAmount': cdr['cost_value'],
                'chargedTaxAmount': cdr['tax_value'],
                'currency': cdr['cost_currency'],
                'customerId': cdr['customer'],
                'appProvider': cdr['provider']
            })

        # Make request
        url = settings.RSS
        if not url.endswith('/'):
            url += '/'

        url += 'rss/cdrs'

        headers = {
            'content-type': 'application/json',
            'X-Nick-Name': settings.STORE_NAME,
            'X-Roles': 'provider',
            'X-Email': settings.WSTOREMAIL
        }

        response = requests.post(url, json=data, headers=headers)

        if response.status_code != 201:
            db = get_database_connection()
            # Restore correlation numbers
            for cdr in cdr_info:
                org = Organization.objects.get(name=cdr['provider'])
                db.wstore_organization.find_and_modify(
                    query={'_id': ObjectId(org.pk)},
                    update={'$inc': {'correlation_number': -1}}
                )['correlation_number']
