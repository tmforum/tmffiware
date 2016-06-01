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

from mock import call

from wstore.store_commons.tests import *
from wstore.admin.users.tests import *

from django.test.client import RequestFactory

from wstore import views


class ServeMediaTestCase(TestCase):
    tags = ('media', )

    def setUp(self):
        # Mock user
        self._user = MagicMock()
        self._user.is_anonymous.return_value = False
        self._org = MagicMock()
        self._user.userprofile.current_organization = self._org

        # Mock Resource model
        views.Resource = MagicMock()
        self._asset_inst = MagicMock()
        self._asset_inst.is_public = False
        self._asset_inst.provider = self._org

        views.Resource.objects.get.return_value = self._asset_inst

        # Mock serve and smart_str
        views.serve = MagicMock()
        views.smart_str = MagicMock()
        views.smart_str.return_value = 'smart string'

        # Mock is_file
        views.os.path.isfile = MagicMock()

        # Mock Order
        views.Order = MagicMock()
        order_inst = MagicMock()
        order_inst.owner_organization = self._org
        views.Order.objects.get.return_value = order_inst

        # Mock offering
        views.Offering = MagicMock()
        self._offering_inst = MagicMock()
        self._offering_inst.asset = self._asset_inst
        views.Offering.objects.get.side_effect = [MagicMock(), self._offering_inst]

    def _validate_res_call(self):
        views.Resource.objects.get.assert_called_once_with(resource_path='media/assets/test_user/widget.wgt')
        self.assertEquals(0, views.Order.objects.get.call_count)

    def _validate_off_call(self):
        self._validate_res_call()
        self.assertEquals([
            call(pk='offpk1'),
            call(pk='offpk2')
        ], views.Offering.objects.get.call_args_list)

    def _validate_order_call(self):
        views.Order.objects.get.assert_called_once_with(pk='111111111111111111111111')
        self.assertEquals(0, views.Resource.objects.get.call_count)

    def _validate_empty_call(self):
        self.assertEquals(0, views.Resource.objects.get.call_count)
        self.assertEquals(0, views.Order.objects.get.call_count)

    def _validate_error(self, response, expected):
        resp = json.loads(response.content)
        self.assertEquals(expected[0], response.status_code)
        self.assertEquals(expected[1], resp)

    def _validate_serve(self, response, expected):
        views.os.path.isfile(expected)
        views.serve.assert_called_once_with(self.request, expected, document_root='/home/test/media/')

    def _validate_xfile(self, response, expected):
        views.os.path.isfile(expected)
        views.smart_str.assert_called_once_with(expected)
        self.assertEquals('smart string', response['X-Sendfile'])

    def _public_asset(self):
        self._asset_inst.is_public = True
        self._asset_inst.provider = MagicMock()

    def _usexfiles(self):
        views.settings = MagicMock()
        views.settings.USE_XSENDFILE = True
        views.settings.MEDIA_ROOT = '/home/test/media/'
        views.settings.MEDIA_URL = '/media/'
        views.settings.MEDIA_DIR = 'media/'

    def _asset_error(self):
        views.Resource.objects.get.side_effect = Exception('Not found')

    def _order_error(self):
        views.Order.objects.get.side_effect = Exception('Not found')

    def _unauthorized(self):
        self._user.userprofile.current_organization = MagicMock()

    def _not_loged(self):
        self._user.is_anonymous.return_value = True

    def _not_found(self):
        views.os.path.isfile.return_value = False

    def _acquired(self):
        self._user.userprofile.current_organization = MagicMock()
        self._user.userprofile.current_organization.acquired_offerings = ['offpk1', 'offpk2']

    @parameterized.expand([
        ('asset', 'assets/test_user', 'widget.wgt', _validate_res_call, _validate_serve, 'assets/test_user/widget.wgt'),
        ('asset_acquired', 'assets/test_user', 'widget.wgt', _validate_off_call, _validate_serve, 'assets/test_user/widget.wgt', _acquired),
        ('public_asset', 'assets/test_user', 'widget.wgt', _validate_res_call, _validate_serve, 'assets/test_user/widget.wgt', _public_asset),
        ('invoice', 'bills', '111111111111111111111111_userbill.pdf', _validate_order_call, _validate_xfile, 'bills/111111111111111111111111_userbill.pdf', _usexfiles),
        ('asset_not_found', 'assets/test_user', 'widget.wgt', _validate_res_call, _validate_error, (404, {
            'result': 'error',
            'error': 'The specified asset does not exists'
        }), _asset_error),
        ('asset_anonymous', 'assets/test_user', 'widget.wgt', _validate_res_call, _validate_error, (401, {
            'result': 'error',
            'error': 'You must be authenticated to download the specified asset'
        }), _not_loged),
        ('asset_unauthorized', 'assets/test_user', 'widget.wgt', _validate_res_call, _validate_error, (403, {
            'result': 'error',
            'error': 'You are not authorized to download the specified asset'
        }), _unauthorized),
        ('invoice_not_found', 'bills', '111111111111111111111111_userbill.pdf', _validate_order_call, _validate_error, (404, {
            'result': 'error',
            'error': 'The specified invoice does not exists'
        }), _order_error),
        ('invoice_anonymous', 'bills', '111111111111111111111111_userbill.pdf', _validate_empty_call, _validate_error, (401, {
            'result': 'error',
            'error': 'You must provide credentials for downloading invoices'
        }), _not_loged),
        ('invoice_unauthorized', 'bills', '111111111111111111111111_userbill.pdf', _validate_order_call, _validate_error, (403, {
            'result': 'error',
            'error': 'You are not authorized to download the specified invoice'
        }), _unauthorized),
        ('file_not_found', 'assets/test_user', 'widget.wgt', _validate_res_call, _validate_error, (404, {
            'result': 'error',
            'error': 'Resource not found'
        }), _not_found),
        ('invalid_type', 'invalid/user', 'widget.wgt', _validate_empty_call, _validate_error, (404, {
            'result': 'error',
            'error': 'Resource not found'
        }))
    ])
    @override_settings(MEDIA_ROOT='/home/test/media/', MEDIA_URL='/media/', MEDIA_DIR='media/')
    def test_serve_media(self, name, path, file_name, call_validator, res_validator, expected, side_effect=None):

        if side_effect is not None:
            side_effect(self)

        factory = RequestFactory()
        self.request = factory.post(
            'media/' + path + '/' + file_name,
            HTTP_ACCEPT='application/json'
        )
        self.request.user = self._user

        media_view = views.ServeMedia(permitted_methods=('GET',))

        response = media_view.read(self.request, path, file_name)
        call_validator(self)
        res_validator(self, response, expected)