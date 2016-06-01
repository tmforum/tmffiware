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

import json
import requests

from django.conf import settings

from wstore.models import Resource


def notify_provider(purchase):
    """
        This method is used to notify the service provider
        that his offering has been purchased
    """
    notification_url = purchase.offering.notification_url

    if not notification_url and not len(purchase.offering.applications):
        return

    # Build common notification data
    data = {
        'offering': {
            'organization': purchase.offering.owner_organization.name,
            'name': purchase.offering.name,
            'version': purchase.offering.version
        },
        'reference': purchase.ref,
    }

    # Include customer info
    if settings.OILAUTH:
        data['customer'] = purchase.owner_organization.actor_id
        data['customer_name'] = purchase.owner_organization.name
    else:
        data['customer'] = purchase.owner_organization.name

    # Notify the service provider
    if notification_url != '':

        data['resources'] = []
        # Include the resources
        for res in purchase.offering.resources:
            resource = Resource.objects.get(pk=res)

            data['resources'].append({
                'name': resource.name,
                'version': resource.version,
                'content_type': resource.content_type,
                'url': resource.get_url()
            })

        body = json.dumps(data)
        headers = {'Content-type': 'application/json'}

        try:
            requests.post(notification_url, data=body, headers=headers, cert=(settings.NOTIF_CERT_FILE, settings.NOTIF_CERT_KEY_FILE))
        except:
            pass
