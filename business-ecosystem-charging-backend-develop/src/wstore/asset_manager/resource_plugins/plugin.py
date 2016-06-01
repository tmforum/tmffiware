# -*- coding: utf-8 -*-

# Copyright (c) 2015 - 2016 CoNWeT Lab., Universidad Politécnica de Madrid

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


class Plugin:

    def on_pre_product_spec_validation(self, provider, asset_t, media_type, url):
        pass

    def on_post_product_spec_validation(self, provider, asset):
        pass

    def on_pre_product_spec_attachment(self, asset, asset_t, product_spec):
        pass

    def on_post_product_spec_attachment(self, asset, asset_t, product_spec):
        pass

    def on_pre_product_offering_validation(self, asset, product_offering):
        pass

    def on_post_product_offering_validation(self, asset, product_offering):
        pass

    def on_product_acquisition(self, asset, contract, order):
        pass
