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

import urllib2
from urlparse import urljoin

from django.db import models
from djangotoolbox.fields import ListField, DictField, EmbeddedModelField

from wstore.models import Organization, Context


# This embedded class is used to save old versions
# of resources to allow downgrades
class ResourceVersion(models.Model):
    version = models.CharField(max_length=20)
    resource_path = models.CharField(max_length=100)
    download_link = models.CharField(max_length=200)


class Resource(models.Model):
    product_id = models.CharField(max_length=100, blank=True, null=True)
    version = models.CharField(max_length=20)  # This field maps the Product Spec version
    provider = models.ForeignKey(Organization)
    content_type = models.CharField(max_length=50)
    download_link = models.URLField()
    resource_path = models.CharField(max_length=100)
    old_versions = ListField(EmbeddedModelField(ResourceVersion))
    state = models.CharField(max_length=20)
    resource_type = models.CharField(max_length=100)
    is_public = models.BooleanField(default=False)
    meta_info = DictField()

    def get_url(self):
        return self.download_link

    def get_uri(self):
        site_context = Context.objects.all()[0]
        base_uri = site_context.site.domain

        return urljoin(base_uri, 'charging/api/assetManagement/assets/' + self.pk)

    class Meta:
        app_label = 'wstore'


class ResourcePlugin(models.Model):
    plugin_id = models.CharField(max_length=100)
    name = models.CharField(max_length=100)
    version = models.CharField(max_length=50)
    author = models.CharField(max_length=100)
    form = DictField()
    module = models.CharField(max_length=200)
    media_types = ListField(models.CharField(max_length=100))
    formats = ListField(models.CharField(max_length=10))
    overrides = ListField(models.CharField(max_length=10))
    options = DictField()

    def __unicode__(self):
        return self.plugin_id

    class Meta:
        app_label = 'wstore'
