# -*- coding: utf-8 -*-

# Copyright (c) 2015 CoNWeT Lab., Universidad Politécnica de Madrid

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


def rollback(post_action=None):
    """
    Make a rollback in case a failure occurs during the execution of a given method
    :param post_action: Callable to be executed as the last step of a rollback
    :return:
    """

    def wrap(method):
        def _remove_file(file_):
            os.remove(file_)

        def _remove_model(model):
            model.delete()

        def wrapper(self, *args, **kwargs):
            # Inject rollback logger
            self.rollback_logger = {
                'files': [],
                'models': []
            }

            try:
                result = method(self, *args, **kwargs)
            except Exception as e:

                # Remove created files
                for file_ in self.rollback_logger['files']:
                    _remove_file(file_)

                # Remove created models
                for model in self.rollback_logger['models']:
                    _remove_model(model)

                if post_action is not None:
                    post_action()

                raise e

            return result

        return wrapper
    return wrap
