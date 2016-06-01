# -*- coding: utf-8 -*-

# Copyright (c) 2013 CoNWeT Lab., Universidad Politécnica de Madrid

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
from decimal import Decimal
from mock import MagicMock, call
from nose_parameterized import parameterized
from urllib2 import HTTPError

from django.test import TestCase
from django.conf import settings

from wstore.store_commons.utils import http
from wstore.store_commons.utils.testing import decorator_mock, build_response_mock, decorator_mock_callable
from wstore.admin.rss import views
from wstore.admin.rss import models
from wstore import models as wstore_models

__test__ = False

class ExpenditureMock():

    _refresh = False

    def ExpenditureManager(self, rss, cred):
        # build object
        return self.ExpAux(self)

    class ExpAux():
        def __init__(self, classCont):
            self._context = classCont

        def set_credentials(self, cred):
            pass

        def set_provider_limit(self):
            if not self._context._refresh:
                self._context._refresh = True
                raise HTTPError('http://rss.test.com', 401, 'Unauthorized', None, None)

        def set_actor_limit(self, limits, userprofile):
            if not self._context._refresh:
                self._context._refresh = True
                raise HTTPError('http://rss.test.com', 401, 'Unauthorized', None, None)


class RSSViewTestCase(TestCase):

    tags = ('rss-view')

    def tearDown(self):
        views.Context = self._old_context
        reload(http)
        reload(views)
        settings.OILAUTH = self._auth
        reload(views)

    def setUp(self):
        http.authentication_required = decorator_mock
        http.supported_request_mime_types = decorator_mock_callable

        reload(views)
        self._old_context = views.Context
        self.views = views
        self.views.build_response = build_response_mock
        self._auth = settings.OILAUTH

        # Create user mock
        self.user = MagicMock()
        self.user.email = "testemail@email.com"
        self.user.userprofile = MagicMock()
        self.user.userprofile.access_token = 'accesstoken'
        self.user.userprofile.refresh_token = 'refreshtoken'
        self.user.is_staff = True

        # Create request mock
        self.request = MagicMock()
        self.request.user = self.user

        # Create context mock
        self.cont_instance = MagicMock()
        self.cont_instance.allowed_currencies = {
            'default': 'EUR',
            'allowed': [{
                'currency': 'EUR'
            }]
        }
        self.cont_instance.is_valid_currency.return_value = True
        self.views.Context = MagicMock()
        self.views.Context.objects = MagicMock()
        self.views.Context.objects.all.return_value = [self.cont_instance]

        # Create RSS mock
        self.rss_object = MagicMock()
        self.views.RSS = MagicMock()
        self.views.RSS.objects = MagicMock()
        self.views.RSS.objects.create.return_value = self.rss_object
        self.views.RSS.objects.get.return_value = self.rss_object
        self.views.RSS.objects.delete = MagicMock()
        self.views.RSS.objects.filter.return_value = []

        settings.OILAUTH = True

        # Mock rss factory
        self.fact_mock = MagicMock()
        self.views.RSSManagerFactory = MagicMock()
        self.views.RSSManagerFactory.return_value = self.fact_mock

        self._exp_mock = MagicMock()
        self._model_mock = MagicMock()
        self._prov_mock = MagicMock(name="ProviderManager")

        self.fact_mock.get_expenditure_manager.return_value = self._exp_mock
        self.fact_mock.get_model_manager.return_value = self._model_mock
        self.fact_mock.get_provider_manager.return_value = self._prov_mock

    # Different side effects that can occur
    def _revoke_staff(self):
        self.user.is_staff = False

    def _existing_rss(self):
        self.views.RSS.objects.all.return_value = [self.rss_object]

    def _invalid_currencies(self):
        self.cont_instance.is_valid_currency.return_value = False

    def _unauthorized(self):
        self._exp_mock.set_provider_limit.side_effect = HTTPError('http://rss.test.com', 401, 'Unauthorized', None, None)

    def _manager_failure(self):
        self._exp_mock.set_provider_limit.side_effect = Exception('Failure')

    def _rss_failure(self):
        http_error = HTTPError('http://rss.test.com', 500, 'Unauthorized', None, None)
        http_error.read = MagicMock()
        http_error.read.return_value = json.dumps({
            'exceptionId': 'SVC1006'
        })

        self._exp_mock.set_provider_limit.side_effect = http_error

    def _generate_models(self, revenue_models=None):
        result = []

        if not revenue_models:
            revenue_models = [{
                'class': 'single-payment',
                'percentage': '10'
            }, {
                'class': 'subscription',
                'percentage': 20
            }, {
                'class': 'use',
                'percentage': '30.00'
            }]

        # Build the expected revenue models
        for model in revenue_models:
            result.append(models.RevenueModel(
                revenue_class=model['class'],
                percentage=Decimal(model['percentage'])
            ))
        return result

    def _model_failure(self):
        self._model_mock.create_revenue_model.side_effect = Exception('The RSS has failed creating the models')

    @parameterized.expand([
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 1
        }, False, (201, 'Created', 'correct'), True, {
            'currency': 'EUR',
            'perTransaction': 10000,
            'weekly': 100000,
            'daily': 10000,
            'monthly': 100000
        }),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'limits': {
                'currency': 'EUR',
                'perTransaction': '10000',
                'daily': '10000',
                'weekly': '10000',
                'monthly': '10000'
            },
            'models': [{
                'class': 'single-payment',
                'percentage': 10.0
            }, {
                'class': 'subscription',
                'percentage': 20.0
            }, {
                'class': 'use',
                'percentage': 30.0
            }]
        }, True, (201, 'Created', 'correct'), True, {
            'currency': 'EUR',
            'perTransaction': 10000.0,
            'weekly': 10000.0,
            'daily': 10000.0,
            'monthly': 10000.0
        }),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 1,
            'limits': {
                'currency': 'EUR'
            }
        }, False, (201, 'Created', 'correct'), True, {
            'currency': 'EUR',
            'perTransaction': 10000,
            'weekly': 100000,
            'daily': 10000,
            'monthly': 100000
        }),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'limits': {
                'weekly': '1000'
            }
        }, False, (201, 'Created', 'correct'), True, {
            'currency': 'EUR',
            'weekly': 1000.0,
        }),
        ({
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'limits': {
                'currency': 'EUR'
            }
        }, False, (400, 'RSS creation error: Missing a required field, name', 'error'), False, {}),
        ({
            'name': 'testrss',
            'api_version': 2,
            'limits': {
                'currency': 'EUR'
            }
        }, False, (400, 'RSS creation error: Missing a required field, host', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'limits': {
                'currency': 'EUR'
            }
        }, False, (400, 'RSS creation error: Missing a required field, api_version', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 3,
            'limits': {
                'currency': 'EUR'
            }
        }, False, (400, 'RSS creation error: Invalid api_version, must be 1 or 2', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2
        }, False, (403, 'Forbidden', 'error'), False, {}, _revoke_staff),
        ({
            'name': 'testrss$',
            'host': 'http://rss.test.com/',
            'api_version': 2
        }, False, (400, 'RSS creation error: Invalid name format', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'invalid_host',
            'api_version': 2
        }, False, (400, 'RSS creation error: Invalid URL format', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2
        }, False, (409, 'RSS creation error: There is a RSS instance already registered', 'error'), False, {}, _existing_rss),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'limits': {
                'currency': 'euro'
            }
        }, False, (400, 'Invalid currency', 'error'), False, {}, _invalid_currencies),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2
        }, False, (401, "You don't have access to the RSS instance requested", 'error'), False, {}, _unauthorized),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 1
        }, False, (400, "The RSS has failed creating the models", 'error'), False, {}, _model_failure),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2
        }, False, (400, 'Failure', 'error'), False, {}, _manager_failure),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2
        }, False, (502, 'The current instance of WStore is not registered in the Revenue Sharing System, so it is not possible to access RSS APIs. Please contact with the administrator of the RSS.', 'error'), False, {}, _rss_failure),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'models': [{
                'percentage': 10.0
            }, {
                'class': 'subscription',
                'percentage': 20.0
            }, {
                'class': 'use',
                'percentage': 30.0
            }]
        }, False, (400, 'Invalid revenue sharing model: Missing a required field', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'models': [{
                'class': 'invalid',
                'percentage': 10.0
            }, {
                'class': 'subscription',
                'percentage': 20.0
            }, {
                'class': 'use',
                'percentage': 30.0
            }]
        }, False, (400, 'Invalid revenue sharing model: Invalid product class', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'models': [{
                'class': 'single-payment',
                'percentage': 10.0
            }, {
                'class': 'subscription',
                'percentage': 'as'
            }, {
                'class': 'use',
                'percentage': 30.0
            }]
        }, False, (400, 'Invalid revenue sharing model: Invalid percentage type', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'models': [{
                'class': 'single-payment',
                'percentage': 10.0
            }, {
                'class': 'subscription',
                'percentage': 150.0
            }, {
                'class': 'use',
                'percentage': 30.0
            }]
        }, False, (400, 'Invalid revenue sharing model: The percentage must be a number between 0 and 100', 'error'), False, {}),
        ({
            'name': 'testrss',
            'host': 'http://rss.test.com/',
            'api_version': 2,
            'models': [{
                'class': 'single-payment',
                'percentage': 10.0
            }, {
                'class': 'use',
                'percentage': 30.0
            }]
        }, False, (400, 'Invalid revenue sharing model: Missing a required product class', 'error'), False, {})
    ])
    def test_rss_creation(self, data, refresh, resp, created, expected_request, side_effect=None):

        # Include data to mock
        self.request.body = json.dumps(data)

        # Mock ExpenditureManager
        self.views.ExpenditureManager = MagicMock()
        # Mock ModelManager
        self.views.ModelManager = MagicMock()

        # Create a mock method to manage the token refresh
        # if needed
        if refresh:
            self._exp_mock = ExpenditureMock()
            self.views.ExpenditureManager = self._exp_mock.ExpenditureManager
            # Create social auth mocks
            social_mock = MagicMock()
            filter_mock = MagicMock()
            object_mock = MagicMock()
            object_mock.extra_data = {
                'access_token': 'accesstoken',
                'refresh_token': 'refreshtoken'
            }
            filter_mock.return_value = [object_mock]
            social_mock.filter = filter_mock
            self.request.user.social_auth = social_mock

        # Create the corresponding side effect if needed
        if side_effect:
            side_effect(self)

        # Call the view
        collection = self.views.RSSCollection(permitted_methods=('GET', 'POST'))
        response = collection.create(self.request)

        # Check response
        val = json.loads(response.content)
        self.assertEquals(response.status_code, resp[0])
        self.assertEquals(val['message'], resp[1])
        self.assertEquals(val['result'], resp[2])

        # Check the result depending if the model should
        # have been created
        if created:
            # Check rss call
            model_info = None
            if 'models' in data:
                model_info = data['models']

            revenue_model = self._generate_models(model_info)
            self.views.RSS.objects.create.assert_called_with(
                name=data['name'],
                host=data['host'],
                api_version=data['api_version'],
                expenditure_limits=expected_request,
                revenue_models=revenue_model,
                aggregator_id=self.user.email
            )

            self.assertEquals(self.rss_object.access_token, self.user.userprofile.access_token)

            views.RSSManagerFactory.assert_called_once_with(self.rss_object)
            self.fact_mock.get_expenditure_manager.assert_called_once_with(self.user.userprofile.access_token)

            if not refresh:
                self._exp_mock.set_provider_limit.assert_called_once_with()

            if data['api_version'] == 2:
                self.fact_mock.get_provider_manager.assert_called_once_with(self.user.userprofile.access_token)

                prov_data = {
                    'provider_id': settings.STORE_NAME.lower() + '-provider',
                    'provider_name': settings.STORE_NAME + '-Provider'
                }

                self._prov_mock.register_provider.assert_called_once_with(prov_data)

        else:
            self.views.RSS.objects.delete.assert_called_once()

    def _not_found(self):
        self.views.RSS.objects.get = MagicMock()
        self.views.RSS.objects.get.side_effect = Exception('Not found')

    def _invalid_data(self):
        self.request.body = None

    def _make_limit_failure(self):
        self.views._make_rss_request.return_value = (True, 502, 'RSS failure')

    @parameterized.expand([
        ({
            'name': 'test',
            'limits': {
                'currency': 'EUR',
                'weekly': 100.0
            }
        }, (200, 'OK', 'correct'), False),
        ({
            'name': 'test',
            'limits': {
                'currency': 'EUR',
                'weekly': 100.0
            },
            'models': [{
                'class': 'single-payment',
                'percentage': 20
            }, {
                'class': 'subscription',
                'percentage': 30
            }, {
                'class': 'use',
                'percentage': 50
            }]
        }, (200, 'OK', 'correct'), False),
        ({}, (200, 'OK', 'correct'), False),
        ({
            'name': 'test',
            'limits': {
                'currency': 'EUR',
                'weekly': 100.0
            }
        }, (404, 'Not found', 'error'), True, _not_found),
        ({
            'name': 'test',
            'limits': {
                'currency': 'EUR',
                'weekly': 100.0
            }
        }, (403, 'Forbidden', 'error'), True, _revoke_staff),
        ({}, (400, 'Invalid JSON data', 'error'), True, _invalid_data),
        ({
            'name': 'existingrss',
            'limits': {
                'currency': 'EUR',
                'weekly': 100.0
            }
        }, (400, 'The selected name is in use', 'error'), True),
        ({
            'name': 'test',
            'limits': {
                'currency': 'EUR',
                'weekly': 100.0
            }
        }, (502, 'RSS failure', 'error'), True, _make_limit_failure),
    ])
    def test_rss_update(self, data, resp, error, side_effect=None):
        # Mock RSS response
        self.views.RSS.objects.filter.return_value = [self.rss_object]

        def get_mock(name=''):
            if name == 'testrss' or name == 'existingrss':
                return self.rss_object
            else:
                raise Exception('')

        self.rss_object.api_version = 1

        self.views.RSS.objects.get = get_mock

        self.request.body = json.dumps(data)

        if 'models' in data:
            # Mock model manager
            model_obj = MagicMock()
            self.views.ModelManager = MagicMock(name="ModelManager")
            self.views.ModelManager.return_value = model_obj
            self.views._check_revenue_models = MagicMock()
            self.views._check_revenue_models.return_value = [{
                'class': 'single-payment',
                'percentage': 20.0
            }, {
                'class': 'subscription',
                'percentage': 30.0
            }, {
                'class': 'use',
                'percentage': 50.0
            }]

        # Mock _make_requests
        self.views._make_rss_request = MagicMock()
        self.views._make_rss_request.return_value = (False, None, None)

        self.views._check_limits = MagicMock()
        self.views._check_limits.return_value = {
            'currency': 'EUR',
            'weekly': 100.0
        }

        if side_effect:
            side_effect(self)

        # Get entry
        entry = self.views.RSSEntry(permitted_methods=('GET', 'PUT', 'DELETE'))

        response = entry.update(self.request, 'testrss')

        # Check response
        val = json.loads(response.content)
        self.assertEquals(response.status_code, resp[0])
        self.assertEquals(val['message'], resp[1])
        self.assertEquals(val['result'], resp[2])

        if not error:
            calls = 0
            if 'limits' in data:
                # Check calls
                self.views._check_limits.assert_called_with(data['limits'])
                calls = calls + 1

            if 'models' in data:
                self.views._check_revenue_models.assert_called_with(data['models'])
                calls = calls + len(data['models'])

            self.assertEquals(self.views._make_rss_request.call_count, calls)

    def test_rss_retrieving(self):
        # Create mocks
        rss_1 = MagicMock()
        rss_1.name = 'test_rss1'
        rss_1.host = 'http://testrss1.org/'
        rss_1.expenditure_limits = {
            'currency': 'EUR',
            'weekly': 100
        }
        rss_1.api_version = 1

        rss_2 = MagicMock()
        rss_2.name = 'test_rss2'
        rss_2.host = 'http://testrss2.org/'
        rss_2.expenditure_limits = {
            'currency': 'EUR',
            'monthly': 500
        }
        rss_2.api_version = 2

        self.views.RSS.objects.all = MagicMock()
        self.views.RSS.objects.all.return_value = [rss_1, rss_2]

        # Create collection
        coll = self.views.RSSCollection(permitted_methods=('GET', 'POST'))

        response = coll.read(self.request)
        val = json.loads(response.content)
        self.assertEquals(response.status_code, 200)
        self.assertEquals(len(val), 2)

        for resp in val:
            if resp['name'] == 'test_rss1':
                self.assertEquals(resp['host'], 'http://testrss1.org/')
                self.assertEquals(resp['api_version'], 1)
                limits = resp['limits']
                self.assertEquals(limits['currency'], 'EUR')
                self.assertEquals(limits['weekly'], 100)
            else:
                self.assertEquals(resp['name'], 'test_rss2')
                self.assertEquals(resp['host'], 'http://testrss2.org/')
                self.assertEquals(resp['api_version'], 2)
                limits = resp['limits']
                self.assertEquals(limits['currency'], 'EUR')
                self.assertEquals(limits['monthly'], 500)

    @parameterized.expand([
        (False,),
        (True,)
    ])
    def test_rss_retrieving_entry(self, failure):

        # Create mocks
        if not failure:
            rss_1 = MagicMock()
            rss_1.name = 'test_rss1'
            rss_1.host = 'http://testrss1.org/'
            rss_1.api_version = 1
            rss_1.expenditure_limits = {
                'currency': 'EUR',
                'weekly': 100
            }
            self.views.RSS.objects.get = MagicMock()
            self.views.RSS.objects.get.return_value = rss_1
        else:
            self.views.RSS.objects.get.side_effect = Exception('failure')

        # Create collection
        entry = self.views.RSSEntry(permitted_methods=('GET', 'PUT', 'DELETE'))

        # Check response
        response = entry.read(self.request, 'test_rss1')
        val = json.loads(response.content)

        if not failure:
            self.views.RSS.objects.get.assert_called_with(name='test_rss1')
            self.assertEquals(response.status_code, 200)

            self.assertEquals(val['name'], 'test_rss1')
            self.assertEquals(val['host'], 'http://testrss1.org/')
            self.assertEquals(val['api_version'], 1)
            limits = val['limits']
            self.assertEquals(limits['currency'], 'EUR')
            self.assertEquals(limits['weekly'], 100)
        else:
            self.assertEquals(response.status_code, 400)
            self.assertEquals(val['message'], 'Invalid request')
            self.assertEquals(val['result'], 'error')

    @parameterized.expand([
        ('test_rss', (204, 'No content', 'correct')),
        ('test_rss1', (404, 'Not found', 'error'), _not_found),
        ('test_rss1', (403, 'Forbidden', 'error'), _revoke_staff),
        ('test_rss1', (502, 'RSS failure', 'error'), _make_limit_failure)
    ])
    def test_rss_deletion(self, name, resp, side_effect=None):
        # create mocks
        self.views._make_rss_request = MagicMock()
        self.views._make_rss_request.return_value = (False, None, None)

        exp_man = MagicMock()
        self.views.ExpenditureManager = MagicMock()
        self.views.ExpenditureManager.return_value = exp_man

        # Create collection
        entry = self.views.RSSEntry(permitted_methods=('GET', 'PUT', 'DELETE'))

        if side_effect:
            side_effect(self)

        # Check response
        response = entry.delete(self.request, name)

        val = json.loads(response.content)
        self.assertEquals(response.status_code, resp[0])
        self.assertEquals(val['message'], resp[1])
        self.assertEquals(val['result'], resp[2])


class RSSModelTestCase(TestCase):

    tags = ('rss-model',)

    def setUp(self):
        # Create RSS entry
        self.rss = models.RSS.objects.create(
            name='test_rss',
            host='http://testhost.com',
        )
        self.rss.access_token = 'aaaaa'
        self.rss.refresh_token = 'bbbbb'
        self.rss.save()

        # Mock userprofile
        self.old_usr = wstore_models.UserProfile

        self.userprofile = MagicMock()
        cred = {
            'access_token': '11111',
            'refresh_token': '22222'
        }
        self.social = MagicMock()
        self.social.extra_data = cred

        self.usr_obj = MagicMock()
        self.usr_obj.user.social_auth.filter.return_value = [self.social]
        self.userprofile.objects.get.return_value = self.usr_obj

        wstore_models.UserProfile = self.userprofile
        TestCase.setUp(self)

    def tearDown(self):
        wstore_models.UserProfile = self.old_usr
        TestCase.tearDown(self)

    def test_refresh_token(self):

        # Call refresh_token method
        error = False
        try:
            self.rss._refresh_token()
        except:
            error = True

        self.assertFalse(error)

        # Check calls
        self.userprofile.objects.get.assert_called_once_with(access_token='aaaaa')
        self.social.refresh_token.assert_called_once_with()

        self.assertEquals(self.usr_obj.access_token, '11111')
        self.assertEquals(self.usr_obj.refresh_token, '22222')
        self.usr_obj.save.assert_called_with()

        self.rss = models.RSS.objects.get(name='test_rss')
        self.assertEquals(self.rss.access_token, '11111')
