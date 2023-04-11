#!/usr/bin/env python
# coding: utf-8

# In[ ]:


from urllib.parse import urlparse
import whois as pythonwhois
import requests

def extract_features(url):
    # Parse the URL to get the domain name and path
    parsed_url = urlparse(url)
    domain = parsed_url.netloc
    path = parsed_url.path

    # Extract URL_LENGTH feature
    url_length = len(url)

    # Extract SERVER feature
    try:
        server = requests.get(url).headers['Server']
    except:
        server = None

    # Extract CONTENT_LENGTH feature
    try:
        content_length = requests.get(url).headers['Content-Length']
    except:
        content_length = None

    # Extract WHOIS_COUNTRY, WHOIS_STATEPRO, WHOIS_REGDATE, WHOIS_UPDATED_DATE features
    try:
        w = pythonwhois.whois(domain)
        whois_country = w.country
        whois_statepro = w.state
        whois_regdate = w.creation_date
        whois_updated_date = w.updated_date
    except:
        whois_country = None
        whois_statepro = None
        whois_regdate = None
        whois_updated_date = None

    # Extract DIST_REMOTE_TCP_PORT, APP_BYTES, SOURCE_APP_PACKETS, REMOTE_APP_PACKETS, SOURCE_APP_BYTES, REMOTE_APP_BYTES features
    try:
        r = requests.get(url)
        dist_remote_tcp_port = r.headers['X-DNS-Prefetch-Control']
        app_bytes = len(r.content)
        source_app_packets = len(r.request.headers)
        remote_app_packets = len(r.headers)
        source_app_bytes = len(str(r.request.headers))
        remote_app_bytes = len(str(r.headers))
    except:
        dist_remote_tcp_port = 0
        app_bytes = 0
        source_app_packets = 0
        remote_app_packets = 0
        source_app_bytes = 0
        remote_app_bytes = 0

    # Return all the extracted features as a dictionary
    features = {
        'URL_LENGTH': url_length,
        'SERVER': server,
        'CONTENT_LENGTH': content_length,
        'WHOIS_COUNTRY': whois_country,
        'WHOIS_STATEPRO': whois_statepro,
        'WHOIS_REGDATE': whois_regdate,
        'WHOIS_UPDATED_DATE': whois_updated_date,
        'DIST_REMOTE_TCP_PORT': dist_remote_tcp_port,
        'APP_BYTES': app_bytes,
        'SOURCE_APP_PACKETS': source_app_packets,
        'REMOTE_APP_PACKETS': remote_app_packets,
        'SOURCE_APP_BYTES': source_app_bytes,
        'REMOTE_APP_BYTES': remote_app_bytes
    }
    return features

