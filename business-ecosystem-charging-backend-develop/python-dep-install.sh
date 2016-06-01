#!/bin/bash
# Download and install Python dependencies
pip install "lxml==3.4.4" "rdflib==4.2.0" "pymongo==3.0.3" "paypalrestsdk==1.11.0"
pip install https://github.com/django-nonrel/django/archive/nonrel-1.6.zip 
pip install https://github.com/django-nonrel/djangotoolbox/archive/master.zip
pip install https://github.com/django-nonrel/mongodb-engine/archive/master.zip
pip install https://github.com/RDFLib/rdflib-jsonld/archive/master.zip

pip install "nose==1.3.6" "django-nose==1.4"

pip install "django-crontab==0.6.0"
pip install "Whoosh==2.7.0"
pip install "Stemming==1.0.1"
pip install requests
pip install requests[security]
pip install regex
