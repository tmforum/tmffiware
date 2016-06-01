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


from __future__ import unicode_literals

import os

from mock import MagicMock
from nose_parameterized import parameterized

from django.test import TestCase
from django.conf import settings
from django.core.management import call_command

from wstore.management.commands import configureproject, loadplugin, removeplugin


__test__ = False

class ConfigureProjectTestCase(TestCase):
    tags = ('management', )

    def setUp(self):
        # Mock stdin
        configureproject.stdin = MagicMock()
        TestCase.setUp(self)

    def tearDown(self):
        reload(configureproject)
        TestCase.tearDown(self)

    @parameterized.expand([
        ('basic', )
    ])
    def test_configure_proyect(self, name):
        pass


class IndexTestCase(TestCase):

    def setUp(self):
        # Mock the standard input
        self.tested_mod.stdin = MagicMock()
        self.tested_mod.stdin.readline.return_value = 'y '
        # Mock rmtree
        self.tested_mod.rmtree = MagicMock()
        TestCase.setUp(self)

    def _invalid_option(self):
        def mock_rl():
            self.tested_mod.stdin.readline = MagicMock()
            self.tested_mod.stdin.readline.return_value = 'y '
            return 't '

        self.tested_mod.stdin.readline = mock_rl

    def _canceled(self):
        self.tested_mod.stdin.readline.return_value = 'n '

    def manager_assertion(self):
        pass

    def _index_tst(self, info, input_=True, side_effect=None, completed=True):

        args = []
        opts = {}
        if not input_:
            args.append('--no-input')

        if side_effect:
            side_effect(self)

        call_command(info['command'], *args, **opts)

        if completed:
            index_path = os.path.join(settings.BASEDIR, 'wstore')
            index_path = os.path.join(index_path, info['module'])
            index_path = os.path.join(index_path, 'indexes')

            # Check calls
            self.tested_mod.rmtree.assert_called_once_with(index_path, True)

            self.manager_assertion()
        else:
            called = True
            try:
                self.tested_mod.rmtree.assert_any_call()
            except:
                called = False

            self.assertFalse(called)


class FakeCommandError(Exception):
    pass


class PluginManagementTestCase(TestCase):

    tags = ('management',)

    def _install_plugin_error(self):
        self.loader_mock.install_plugin.side_effect = Exception('Error installing plugin')

    def _unistall_plugin_error(self):
        self.loader_mock.uninstall_plugin.side_effect = Exception('Error uninstalling plugin')

    def _check_loaded(self):
        self.loader_mock.install_plugin.assert_called_once_with('test_plugin.zip')

    def _check_removed(self):
        self.loader_mock.uninstall_plugin.assert_called_once_with('test_plugin')

    def _test_plugin_command(self, module, module_name, args, checker=None, side_effect=None, err_msg=None):
        # Create Mocks
        module.stout = MagicMock()
        module.CommandError = FakeCommandError
        module.PluginLoader = MagicMock(name="PluginLoader")

        self.loader_mock = MagicMock()
        module.PluginLoader.return_value = self.loader_mock

        if side_effect is not None:
            side_effect(self)

        opts = {}
        error = None
        try:
            call_command(module_name, *args, **opts)
        except Exception as e:
            error = e

        if err_msg is None:
            self.assertEquals(error, None)
            checker(self)
        else:
            self.assertTrue(isinstance(error, FakeCommandError))
            self.assertEquals(unicode(e), err_msg)

    @parameterized.expand([
        ('correct', ['test_plugin.zip'], _check_loaded),
        ('inv_argument', ['test1', 'test2'], None,  None, "Error: Please specify the path to the plugin package"),
        ('exception', ['test_plugin.zip'], None, _install_plugin_error, 'Error installing plugin')
    ])
    def test_load_plugin(self, name, args, checker=None, side_effect=None, err_msg=None):
        self._test_plugin_command(loadplugin, 'loadplugin', args, checker, side_effect, err_msg)

    @parameterized.expand([
        ('correct', ['test_plugin'], _check_removed),
        ('inv_argument_mul', ['test1', 'test2'], None, None, "Error: Please specify only one plugin to be deleted"),
        ('inv_argument', [], None, None, "Error: Please specify the plugin to be deleted"),
        ('exception', ['test_plugin'], None, _unistall_plugin_error, 'Error uninstalling plugin')
    ])
    def test_remove_plugin(self, name, args, checker=None, side_effect=None, err_msg=None):
        self._test_plugin_command(removeplugin, 'removeplugin', args, checker, side_effect, err_msg)
