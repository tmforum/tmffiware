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

from __future__ import unicode_literals

from decimal import Decimal

from django.contrib.auth.models import User
from django.contrib.sites.models import Site
from django.db.models.signals import post_save
from djangotoolbox.fields import ListField
from djangotoolbox.fields import DictField, EmbeddedModelField

from wstore.admin.rss.models import *
from wstore.charging_engine.models import *


class Context(models.Model):

    site = models.OneToOneField(Site, related_name='site')
    local_site = models.OneToOneField(Site, related_name='local_site', null=True, blank=True)
    top_rated = ListField()
    newest = ListField()
    user_refs = DictField()
    allowed_currencies = DictField()
    revenue_models = ListField(EmbeddedModelField(RevenueModel))

    def is_valid_currency(self, currency):
        """
         Checks that a currency is valid for WStore
       """
        valid = False
        if 'allowed' in self.allowed_currencies and len(self.allowed_currencies['allowed']) > 0:
            for c in self.allowed_currencies['allowed']:
                if c['currency'].lower() == currency.lower():
                    valid = True
                    break
        return valid


class Organization(models.Model):

    name = models.CharField(max_length=50, unique=True)
    notification_url = models.CharField(max_length=300, null=True, blank=True)
    acquired_offerings = ListField()
    private = models.BooleanField(default=True)
    correlation_number = models.IntegerField(default=0)
    tax_address = DictField()
    managers = ListField()
    actor_id = models.CharField(null=True, blank=True, max_length=100)

    expenditure_limits = DictField()

    def has_rated_offering(self, user, offering):
        """
        Check if the user has rated an offering on behalf the
        organization
        """
        found = False
        for rate in self.rated_offerings:
            if rate['user'] == user.pk and rate['offering'] == offering.pk:
                found = True
                break

        return found


from wstore.asset_manager.models import Resource, ResourcePlugin


class UserProfile(models.Model):

    user = models.OneToOneField(User)
    organizations = ListField()
    current_organization = models.ForeignKey(Organization)
    complete_name = models.CharField(max_length=100)
    actor_id = models.CharField(null=True, blank=True, max_length=100)

    access_token = models.CharField(max_length=150, null=True, blank=True)

    def get_current_roles(self):
        roles = []
        for o in self.organizations:
            if o['organization'] == self.current_organization.pk:
                roles = o['roles']
                break

        return roles

    def is_user_org(self):

        result = False
        # Use the actor_id for identify the user organization
        # in order to avoid problems with nickname changes
        if self.actor_id and self.current_organization.actor_id:
            if self.actor_id == self.current_organization.actor_id:
                result = True
        else:
            if self.user.username == self.current_organization.name:
                result = True

        return result


def create_user_profile(sender, instance, created, **kwargs):

    if created:
        # Create a private organization for the user
        default_organization = Organization.objects.get_or_create(name=instance.username)
        default_organization[0].managers.append(instance.pk)
        default_organization[0].save()

        profile, created = UserProfile.objects.get_or_create(
            user=instance,
            organizations=[{
                'organization': default_organization[0].pk,
                'roles': ['customer', 'developer']
            }],
            current_organization=default_organization[0]
        )
        if instance.first_name and instance.last_name:
            profile.complete_name = instance.first_name + ' ' + instance.last_name
            profile.save()


def create_context(sender, instance, created, **kwargs):

    if created:
        if not len(Context.objects.all()):
            context = Context.objects.get_or_create(site=instance)[0]
            context.allowed_currencies = {
                'allowed': [{
                    'currency': 'EUR',
                    'in_use': False
                }],
                'default': 'EUR'
            }

            from wstore.admin.rss.views import build_db_models
            db_models = build_db_models([{
                'class': 'single-payment',
                'percentage': Decimal('10.0')
            }, {
                'class': 'subscription',
                'percentage': Decimal('20.0')
            }, {
                'class': 'use',
                'percentage': Decimal('30.0')
            }])

            # Create default revenue models
            context.revenue_models = db_models
            context.save()
        else:
            context = Context.objects.all()[0]
            context.local_site = instance
            context.save()


# Creates a new user profile when an user is created
post_save.connect(create_user_profile, sender=User)


# Creates a context when the site is created
post_save.connect(create_context, sender=Site)
