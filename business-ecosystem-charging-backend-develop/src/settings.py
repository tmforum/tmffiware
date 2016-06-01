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


from os import path

DEBUG = True
TEMPLATE_DEBUG = DEBUG

ADMINS = (
    # ('Your Name', 'your_email@example.com'),
)

MANAGERS = ADMINS

DATABASES = {
    'default': {
        'ENGINE': 'django_mongodb_engine',  # Add 'postgresql_psycopg2', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': 'wstore_db',           # Or path to database file if using sqlite3.
        'USER': '',                         # Not used with sqlite3.
        'PASSWORD': '',                     # Not used with sqlite3.
        'HOST': '',                         # Set to empty string for localhost. Not used with sqlite3.
        'PORT': '',                         # Set to empty string for default. Not used with sqlite3.
        'TEST_NAME': 'test_database',
    }
}

BASEDIR = path.dirname(path.abspath(__file__))

STORE_NAME = 'WStore'
AUTH_PROFILE_MODULE = 'wstore.models.UserProfile'

ADMIN_ROLE = 'provider'
PROVIDER_ROLE = 'seller'
CUSTOMER_ROLE = 'customer'

SITE_ID=u''

# Absolute filesystem path to the directory that will hold user-uploaded files.
MEDIA_DIR = 'media/'
MEDIA_ROOT = path.join(BASEDIR, MEDIA_DIR)
BILL_ROOT = path.join(MEDIA_ROOT, 'bills')

# URL that handles the media served from MEDIA_ROOT.
MEDIA_URL = '/charging/media/'

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.messages',
    'django.contrib.admin',
    'django_mongodb_engine',
    'djangotoolbox',
    'wstore',
    'wstore.store_commons',
    'wstore.charging_engine',
    'django_crontab',
    'django_nose'
)

# Make this unique, and don't share it with anybody.
SECRET_KEY = '8p509oqr^68+z)y48_*pv!ceun)gu7)yw6%y9j2^0=o14)jetr'

TEMPLATE_CONTEXT_PROCESSORS = (
    'django.contrib.auth.context_processors.auth',
    'django.core.context_processors.debug',
    'django.core.context_processors.i18n',
    'django.core.context_processors.media',
    'django.core.context_processors.request',
    'django.core.context_processors.static',
)

# List of callables that know how to import templates from various sources.
TEMPLATE_LOADERS = (
    'django.template.loaders.filesystem.Loader',
    'django.template.loaders.app_directories.Loader',
)

MIDDLEWARE_CLASSES = (
    'wstore.store_commons.middleware.URLMiddleware',
)

WSTOREMAILUSER = 'email_user'
WSTOREMAIL = 'wstore_email'
WSTOREMAILPASS = 'wstore_email_passwd'
SMTPSERVER = 'wstore_smtp_server'

WSTOREPROVIDERREQUEST = 'provider_requ_email'

URL_MIDDLEWARE_CLASSES = {
    'default': (
        'django.middleware.common.CommonMiddleware',
        'django.contrib.sessions.middleware.SessionMiddleware',
        'wstore.store_commons.middleware.ConditionalGetMiddleware',
        'django.contrib.auth.middleware.AuthenticationMiddleware',
        'django.contrib.messages.middleware.MessageMiddleware',
    ),
    'api': (
        'django.middleware.common.CommonMiddleware',
        'django.contrib.sessions.middleware.SessionMiddleware',
        'wstore.store_commons.middleware.ConditionalGetMiddleware',
        'wstore.store_commons.middleware.AuthenticationMiddleware',
    ),
    'media': (
        'django.middleware.common.CommonMiddleware',
        'django.contrib.sessions.middleware.SessionMiddleware',
        'wstore.store_commons.middleware.ConditionalGetMiddleware',
        'wstore.store_commons.middleware.AuthenticationMiddleware',
    )
}

ROOT_URLCONF = 'urls'

# Python dotted path to the WSGI application used by Django's runserver.
WSGI_APPLICATION = 'wsgi.application'

# Payment method determines the payment gateway to be used
# Allowed values: paypal, fipay, None (default)
PAYMENT_METHOD = None

ACTIVATION_DAYS = 2

AUTHENTICATION_BACKENDS = (
    'django.contrib.auth.backends.ModelBackend',
)

TEST_RUNNER = 'django_nose.NoseTestSuiteRunner'

# Daily job that checks pending pay-per-use charges
CRONJOBS = [
    ('0 5 * * *', 'django.core.management.call_command', ['resolve_use_charging']),
]

# Hack to ignore `site` instance creation
# This will prevent site creation on syncdb
from django.db.models import signals
from django.contrib.sites.management import create_default_site
from django.contrib.sites import models as site_app

signals.post_syncdb.disconnect(create_default_site, site_app)

CLIENTS = {
    'paypal': 'wstore.charging_engine.payment_client.paypal_client.PayPalClient',
    'fipay': 'wstore.charging_engine.payment_client.fipay_client.FiPayClient',
    None: 'wstore.charging_engine.payment_client.payment_client.PaymentClient'
}

PAYMENT_CLIENT = CLIENTS[PAYMENT_METHOD]

RESOURCE_INDEX_DIR = path.join(BASEDIR, path.join('wstore', path.join('admin', 'indexes')))

NOTIF_CERT_FILE = None
NOTIF_CERT_KEY_FILE = None

from services_settings import *