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

import os
import smtplib
from email import encoders
from email.mime.text import MIMEText
from email.mime.base import MIMEBase
from email.mime.multipart import MIMEMultipart
from urlparse import urljoin

from django.conf import settings
from django.core.exceptions import ImproperlyConfigured

from wstore.models import User, Context


class NotificationsHandler:

    def __init__(self):
        # Read email configuration
        self._mailuser = settings.WSTOREMAILUSER
        self._password = settings.WSTOREMAILPASS
        self._fromaddr = settings.WSTOREMAIL
        self._server = settings.SMTPSERVER

        if not len(self._mailuser) or not len(self._password) or not len(self._fromaddr) or not len(self._server):
            raise ImproperlyConfigured('Missing email configuration')

    def _send_email(self, recipient, msg):
        server = smtplib.SMTP(self._server)
        server.starttls()
        server.login(self._mailuser, self._password)

        server.sendmail(self._fromaddr, recipient, msg.as_string())

    def _send_text_email(self, text, recipients, subject):
        msg = MIMEText(text)
        msg['Subject'] = subject
        msg['From'] = self._fromaddr
        msg['To'] = ','.join(recipients)

        self._send_email(recipients, msg)

    def _send_multipart_email(self, text, recipients, subject, bills):
        msg = MIMEMultipart()
        msg['Subject'] = subject
        msg['From'] = self._fromaddr
        msg['To'] = ','.join(recipients)

        message = MIMEText(text)
        msg.attach(message)

        for bill in bills:
            path = os.path.join(settings.BASEDIR, bill[1:])
            fp = open(path, 'rb')

            b_msg = MIMEBase('application', 'pdf')
            b_msg.set_payload(fp.read())
            fp.close()
            # Encode the payload using Base64
            encoders.encode_base64(b_msg)
            b_msg.add_header('Content-Disposition', 'attachment', filename=bill.split('/')[-1])
            msg.attach(b_msg)

        self._send_email(recipients, msg)

    def send_acquired_notification(self, order):
        org = order.owner_organization
        recipients = [User.objects.get(pk=pk).email for pk in org.managers]
        domain = Context.objects.all()[0].site.domain

        order_url = urljoin(domain, '/#/inventory/order')
        product_url = urljoin(domain, '/#/inventory/product')

        text = 'We have received the payment of your order with reference ' + order.pk + '\n'
        text += 'containing the following product offerings: \n\n'
        for cont in order.contracts:
            text += cont.offering.name + ' with id ' + cont.offering.off_id + '\n\n'

        text += 'You can review your orders at: \n' + order_url + '\n'
        text += 'and your acquired products at: \n' + product_url + '\n'

        self._send_multipart_email(text, recipients, 'Product order accepted', order.bills)

    def send_provider_notification(self, order, contract):
        # Get destination email
        org = contract.offering.owner_organization
        recipients = [User.objects.get(pk=pk).email for pk in org.managers]
        domain = Context.objects.all()[0].site.domain

        url = urljoin(domain, '/#/inventory/order')

        text = 'Your product offering with name ' + contract.offering.name + ' and id ' + contract.offering.off_id +'\n'
        text += 'has been acquired by the user ' + order.owner_organization.name + '\n'
        text += 'Please review you pending orders at: \n\n' + url

        self._send_text_email(text, recipients, 'Product offering acquired')

    def send_payment_required_notification(self, order, contract):
        org = order.owner_organization
        recipients = [User.objects.get(pk=pk).email for pk in org.managers]

        domain = Context.objects.all()[0].site.domain
        url = urljoin(domain, '/#/inventory/order/' + order.order_id)

        text = 'Your subscription belonging to the product offering ' + contract.offering.name + ' has expired.\n'
        text += 'You can renovate all your pending subscriptions of the order with reference ' + order.pk + '\n'
        text += 'in the web portal or accessing the following link: \n\n'
        text += url

        self._send_text_email(text, recipients, contract.offering.name + ' subscription expired')

    def send_near_expiration_notification(self, order, contract, days):
        org = order.owner_organization
        recipients = [User.objects.get(pk=pk).email for pk in org.managers]

        domain = Context.objects.all()[0].site.domain
        url = urljoin(domain, '/#/inventory/order/' + order.order_id)

        text = 'Your subscription belonging to the product offering ' + contract.offering.name + '\n'
        text += 'is going to expire in ' + unicode(days) + ' days. \n\n'
        text += 'You can renovate all your pending subscriptions of the order with reference ' + order.pk + '\n'
        text += 'in the web portal or accessing the following link: \n\n'
        text += url

        self._send_text_email(text, recipients, contract.offering.name + ' subscription is about to expire')

    def send_renovation_notification(self, order, transactions):
        org = order.owner_organization
        recipients = [User.objects.get(pk=pk).email for pk in org.managers]
        domain = Context.objects.all()[0].site.domain

        order_url = urljoin(domain, '/#/inventory/order')
        product_url = urljoin(domain, '/#/inventory/product')

        text = 'We have received your recurring payment for renovating products offerings\n'
        text += 'acquired in the order with reference ' + order.pk + '\n'

        text += 'The following product offerings have been renovated: \n\n'
        for t in transactions:
            cont = order.get_item_contract(t['item'])
            text += cont.offering.name + ' with id ' + cont.offering.off_id + '\n\n'

        text += 'You can review your orders at: \n' + order_url + '\n'
        text += 'and your acquired products at: \n' + product_url + '\n'

        self._send_multipart_email(text, recipients, 'Product order accepted', order.bills[-len(transactions):])
