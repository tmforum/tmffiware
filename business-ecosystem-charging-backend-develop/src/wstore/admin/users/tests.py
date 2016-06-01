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

from copy import deepcopy
from django.core.exceptions import ImproperlyConfigured

import json
from mock import MagicMock, mock_open, call
from nose_parameterized import parameterized

from django.test import TestCase

from wstore.admin.users import views, notification_handler
from wstore.store_commons.utils import http
from wstore.store_commons.utils.testing import decorator_mock, decorator_mock_callable

__test__ = False


class UserEntryTestCase(TestCase):

    tags = ('user-admin',)

    @classmethod
    def setUpClass(cls):
        # Mock class decorators
        http.authentication_required = decorator_mock
        http.supported_request_mime_types = decorator_mock_callable
        reload(views)

        super(UserEntryTestCase, cls).setUpClass()

    @classmethod
    def tearDownClass(cls):
        # Restore mocked decorators
        reload(http)
        reload(views)
        super(UserEntryTestCase, cls).tearDownClass()

    def setUp(self):

        # Create mock request
        user_object = MagicMock()
        user_object.is_staff = False
        user_object.pk = '2222'
        user_object.userprofile.actor_id = 2
        user_object.username = 'test_user'

        self.request = MagicMock()
        self.request.META.get.return_value = 'application/json'
        self.request.user = user_object

        # Mock user
        views.User = MagicMock()
        views.User.objects.get = MagicMock()
        views.User.objects.get.return_value = user_object

        views.Organization = MagicMock()
        self.org = MagicMock()
        self.org.name = 'test_org'
        self.org.managers = []
        views.Organization.objects.get.return_value = self.org

    def _basic_user(self):
        self.request.user.userprofile.complete_name = 'Test user'
        self.request.user.userprofile.current_organization.name = 'test_user'
        self.request.user.userprofile.organizations = [{
            'organization': '11111',
            'roles': ['customer']
        }]
        self.request.user.userprofile.current_organization.tax_address = {
            'street': 'fakestreet'
        }
        self.request.user.userprofile.get_current_roles.return_value = ['provider']

    def _org_manager_staff(self):
        self._basic_user()
        self.request.user.userprofile.is_user_org.return_value = False
        self.org.managers = ['2222']
        self.request.user.is_staff = True

    def _forbidden(self):
        self.request.user.username = 'invalid'

    @parameterized.expand([
        ('basic', _basic_user, 200, {
            'href': 'http://domain.com/charging/api/userManagement/users/test_user',
            'id': 'test_user',
            'completeName': 'Test user',
            'currentOrganization': 'test_user',
            'organizations': [{
                'name': 'test_org',
                'roles': ['customer']
            }],
            'currentRoles': ['provider'],
            'billingAddress': {
                'street': 'fakestreet'
            }
        }),
        ('org_manager_staff', _org_manager_staff, 200, {
            'href': 'http://domain.com/charging/api/userManagement/users/test_user',
            'id': 'test_user',
            'completeName': 'Test user',
            'currentOrganization': 'test_user',
            'organizations': [{
                'name': 'test_org',
                'roles': ['customer', 'manager']
            }],
            'currentRoles': ['provider', 'admin'],
            'billingAddress': {
                'street': 'fakestreet'
            }
        }),
        ('forbidden', _forbidden, 403, {
            'result': 'error',
            'error': 'You are not authorized to retrieve user info'
        })
    ])
    def test_get_user(self, name, user_filler, status, response):
        # Mock context
        views.Context = MagicMock()
        cntx_instance = MagicMock()
        cntx_instance.site.domain = 'http://domain.com'
        views.Context.objects.all.return_value = [cntx_instance]

        user_filler(self)

        user_entry = views.UserProfileEntry(permitted_methods=('GET', 'PATCH'))

        result = user_entry.read(self.request, 'test_user')

        # Check result
        self.assertEquals(status, result.status_code)
        self.assertEquals(response, json.loads(result.content))

        if status == 200:
            # Check calls
            views.Context.objects.all.assert_called_once_with()
            views.Organization.objects.get.assert_called_once_with(pk='11111')

    def _invalid_data(self):
        self.request.body = 'invalid'

    def _incomplete_profile(self):
        self.org.tax_address = {}

    CORRECT_RES = {
        'result': 'correct',
        'message': 'OK'
    }

    @parameterized.expand([
        ('complete', {
            'billingAddress': {
                'street': 'fakestreet',
                'postal': '12345',
                'city': 'a city',
                'province': 'a province',
                'country': 'a country'
            }
        }, 200, CORRECT_RES),
        ('street', {
            'billingAddress': {
                'street': 'fakestreet'
            }
        }, 200, CORRECT_RES),
        ('postal', {
            'billingAddress': {
                'postal': '12345'
            }
        }, 200, CORRECT_RES),
        ('city', {
            'billingAddress': {
                'city': 'a city',
            }
        }, 200, CORRECT_RES),
        ('province', {
            'billingAddress': {
                'province': 'a province'
            }
        }, 200, CORRECT_RES),
        ('country', {
            'billingAddress': {
                'country': 'a country'
            }
        }, 200, CORRECT_RES),
        ('none', {}, 200, CORRECT_RES),
        ('forbidden', {}, 403, {
            'result': 'error',
            'error': 'You are not authorized to update user info'
        }, _forbidden),
        ('invalid_data', {}, 400, {
            'result': 'error',
            'error': 'Invalid JSON content'
        }, _invalid_data),
        ('incomplete',  {
            'billingAddress': {
                'province': 'a province'
            }
        }, 400, {
            'result': 'error',
            'error': 'Incomplete billing address, there is a missing field'
        }, _incomplete_profile)
    ])
    def test_user_update(self, name, data, status, response, side_effect=None):

        initial_address = {
            'street': 'initialstr',
            'postal': 'initialpost',
            'city': 'initialcity',
            'province': 'initialprovince',
            'country': 'initialcountry'
        }

        self.org.tax_address = deepcopy(initial_address)

        # Include data request
        self.request.body = json.dumps(data)

        # Create view class
        user_entry = views.UserProfileEntry(permitted_methods=('GET', 'PATCH'))

        # Create side effect if needed
        if side_effect:
            side_effect(self)

        # Call the view
        result = user_entry.patch(self.request, 'test_user')

        # Check result
        self.assertEquals(status, result.status_code)
        self.assertEquals(response, json.loads(result.content))

        # Check modified profile
        if status == 200 and 'billingAddress' in data:
            expected = initial_address.copy()
            expected.update(data['billingAddress'])

            self.assertEquals(expected, self.org.tax_address)
            self.org.save.assert_called_once_with()
        else:
            # Check that userprofile has not been modified
            if 'billingAddress' not in data:
                self.assertEquals(initial_address, self.org.tax_address)

            self.assertFalse(self.org.save.called)


class NotificationsTestCase(TestCase):
    tags = ('notifications', )

    def setUp(self):
        # Mock email configuration
        notification_handler.settings.WSTOREMAIL = 'wstore@email.com'
        notification_handler.settings.WSTOREMAILPASS = 'passwd'
        notification_handler.settings.WSTOREMAILUSER = 'wstore'
        notification_handler.settings.SMTPSERVER = 'smtp.gmail.com'

        notification_handler.settings.BASEDIR = '/home/test/wstore'

        # Mock contracts
        contract1 = MagicMock()
        contract1.offering.name = 'Offering1'
        contract1.offering.off_id = '1'
        contract1.offering.owner_organization.managers = ['33333', '44444']

        contract2 = MagicMock()
        contract2.offering.name = 'Offering2'
        contract2.offering.off_id = '2'

        # Mock order
        self._order = MagicMock()
        self._order.pk = 'orderid'
        self._order.order_id = '67'
        self._order.owner_organization.managers = ['11111', '22222']
        self._order.owner_organization.name = 'customer'
        self._order.bills = ['/media/bills/bill1.pdf']
        self._order.get_item_contract.return_value = contract1

        self._order.contracts = [contract1, contract2]

        # Mock user
        notification_handler.User = MagicMock()
        self._user1 = MagicMock()
        self._user1.email = 'user1@email.com'
        self._user2 = MagicMock()
        self._user2.email = 'user2@email.com'
        notification_handler.User.objects.get.side_effect = [self._user1, self._user2]

        # Mock context
        notification_handler.Context = MagicMock()
        context = MagicMock()
        context.site.domain = 'http://localhost:8000'
        notification_handler.Context.objects.all.return_value = [context]

        # Mock email libs
        notification_handler.MIMEMultipart = MagicMock()
        notification_handler.MIMEText = MagicMock()
        notification_handler.MIMEBase = MagicMock()
        notification_handler.encoders = MagicMock()
        notification_handler.smtplib = MagicMock()

        # Mock open method
        self._mock_open = mock_open()
        self._old_open = notification_handler.__builtins__['open']
        notification_handler.__builtins__['open'] = self._mock_open

    def tearDown(self):
        notification_handler.__builtins__['open'] = self._old_open
        reload(notification_handler)

    def _empty_email(self):
        notification_handler.settings.WSTOREMAIL = ''

    def _empty_pass(self):
        notification_handler.settings.WSTOREMAILPASS = ''

    def _empty_user(self):
        notification_handler.settings.WSTOREMAILUSER = ''

    def _empty_server(self):
        notification_handler.settings.SMTPSERVER = ''

    @parameterized.expand([
        (_empty_email, ),
        (_empty_pass, ),
        (_empty_user, ),
        (_empty_server, )
    ])
    def test_improperly_configured(self, empty_param):
        empty_param(self)

        error = None
        try:
            notification_handler.NotificationsHandler()
        except ImproperlyConfigured as e:
            error = e

        self.assertTrue(error is not None)
        self.assertEquals('Missing email configuration', unicode(error))

    def _validate_user_call(self):
        self.assertEquals([
            call(pk='11111'),
            call(pk='22222')
        ], notification_handler.User.objects.get.call_args_list)

    def _validate_provider_call(self):
        self.assertEquals([
            call(pk='33333'),
            call(pk='44444')
        ], notification_handler.User.objects.get.call_args_list)

    def _validate_mime_text_info(self, subject):
        self.assertEquals([
            call('Subject', subject),
            call('From', 'wstore@email.com'),
            call('To', 'user1@email.com,user2@email.com')
        ], notification_handler.MIMEText().__setitem__.call_args_list)

    def _validate_email_call(self, mime):
        notification_handler.smtplib.SMTP.assert_called_once_with('smtp.gmail.com')
        notification_handler.smtplib.SMTP().starttls.assert_called_once_with()
        notification_handler.smtplib.SMTP().login.assert_called_once_with('wstore', 'passwd')
        notification_handler.smtplib.SMTP().sendmail.assert_called_once_with(
            'wstore@email.com',
            ['user1@email.com', 'user2@email.com'],
            mime().as_string()
        )

    def _validate_multipart_call(self):
        self._mock_open.assert_called_once_with('/home/test/wstore/media/bills/bill1.pdf', 'rb')

        notification_handler.MIMEBase.assert_called_once_with('application', 'pdf')
        notification_handler.MIMEBase().set_payload.assert_called_once_with(self._mock_open().read())
        self._mock_open().close.assert_called_once_with()

        notification_handler.encoders.encode_base64.assert_called_once_with(notification_handler.MIMEBase())
        notification_handler.MIMEBase().add_header.assert_called_once_with(
            'Content-Disposition',
            'attachment',
            filename='bill1.pdf'
        )

        self.assertEquals([
            call(notification_handler.MIMEText()),
            call(notification_handler.MIMEBase())
        ], notification_handler.MIMEMultipart().attach.call_args_list)

    def test_acquisition_notification(self):
        # Execute method
        handler = notification_handler.NotificationsHandler()
        handler.send_acquired_notification(self._order)

        # Validate calls
        self._validate_user_call()

        notification_handler.MIMEMultipart.assert_called_once_with()

        self.assertEquals([
            call('Subject', 'Product order accepted'),
            call('From', 'wstore@email.com'),
            call('To', 'user1@email.com,user2@email.com')
        ], notification_handler.MIMEMultipart().__setitem__.call_args_list)

        text = "We have received the payment of your order with reference orderid\n"
        text += "containing the following product offerings: \n\n"
        text += "Offering1 with id 1\n\n"
        text += "Offering2 with id 2\n\n"
        text += "You can review your orders at: \nhttp://localhost:8000/#/inventory/order\n"
        text += "and your acquired products at: \nhttp://localhost:8000/#/inventory/product\n"

        notification_handler.MIMEText.assert_called_once_with(text)

        self._validate_multipart_call()
        self._validate_email_call(notification_handler.MIMEMultipart)

    def test_renovation_notification(self):
        handler = notification_handler.NotificationsHandler()
        transactions = [{
            'item': '0'
        }]

        handler.send_renovation_notification(self._order, transactions)

        self._validate_user_call()
        self._order.get_item_contract.assert_called_once_with('0')

        text = 'We have received your recurring payment for renovating products offerings\n'
        text += 'acquired in the order with reference orderid\n'
        text += 'The following product offerings have been renovated: \n\n'
        text += 'Offering1 with id 1\n\n'
        text += 'You can review your orders at: \nhttp://localhost:8000/#/inventory/order\n'
        text += 'and your acquired products at: \nhttp://localhost:8000/#/inventory/product\n'

        notification_handler.MIMEText.assert_called_once_with(text)

        self._validate_multipart_call()
        self._validate_email_call(notification_handler.MIMEMultipart)

    def test_provider_notification(self):
        handler = notification_handler.NotificationsHandler()
        handler.send_provider_notification(self._order, self._order.contracts[0])

        # Validate calls
        self._validate_provider_call()

        text = 'Your product offering with name Offering1 and id 1\n'
        text += 'has been acquired by the user customer\n'
        text += 'Please review you pending orders at: \n\nhttp://localhost:8000/#/inventory/order'

        notification_handler.MIMEText.assert_called_once_with(text)

        self._validate_mime_text_info('Product offering acquired')

        self._validate_email_call(notification_handler.MIMEText)

    def test_payment_required_notification(self):
        handler = notification_handler.NotificationsHandler()
        handler.send_payment_required_notification(self._order, self._order.contracts[0])

        self._validate_user_call()

        text = 'Your subscription belonging to the product offering Offering1 has expired.\n'
        text += 'You can renovate all your pending subscriptions of the order with reference orderid\n'
        text += 'in the web portal or accessing the following link: \n\n'
        text += 'http://localhost:8000/#/inventory/order/67'

        notification_handler.MIMEText.assert_called_once_with(text)

        self._validate_mime_text_info('Offering1 subscription expired')

        self._validate_email_call(notification_handler.MIMEText)

    def test_near_expiration_notification(self):
        handler = notification_handler.NotificationsHandler()
        handler.send_near_expiration_notification(self._order, self._order.contracts[0], 3)

        self._validate_user_call()

        text = 'Your subscription belonging to the product offering Offering1\n'
        text += 'is going to expire in 3 days. \n\n'
        text += 'You can renovate all your pending subscriptions of the order with reference orderid\n'
        text += 'in the web portal or accessing the following link: \n\n'
        text += 'http://localhost:8000/#/inventory/order/67'

        notification_handler.MIMEText.assert_called_once_with(text)

        self._validate_mime_text_info('Offering1 subscription is about to expire')
