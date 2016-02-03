#!/usr/bin/env python3
#
# Tested with python 3.4
#
# This script will read json-encoded auditlog messages from Transmart and send them as HTTP POST messages to
# metrics.pfizer.com.

import sys
import json
import concurrent.futures
import urllib.request
import urllib.parse
from urllib.request import Request
# httpagentparser is not in the standard library, use "pip3 install httpagentparser" to install it
import httpagentparser

url = 'http://metrics.pfizer.com/metrics/servlet/RegisterEventServlet?'
TIMEOUT = 4 #seconds


failcount = 0

def send_url(msg):
    global failcount
    
    action = [msg.get('action')] or [msg.get('study'), msg.get('subset1'), msg.get('subset2')]
    action = '|'.join([i for i in action if i])
    args = dict(action = action,
                application = msg['program'],
                appVersion = msg['programVersion'],
                user = msg['user'],
                task = msg['event'],
                browser = httpagentparser.detect(msg['userAgent'])['browser']['name']
            )
    fullurl = url + urllib.parse.urlencode(args)
    #print(fullurl)
    try:
        urllib.request.urlopen(Request(fullurl, method='POST'), timeout=TIMEOUT).readall()
        failcount = 0
    except Exception as e:
        failcount += 1
        if failcount > 15:
            raise
        print(e, file=sys.stderr)


with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:
    for line in sys.stdin:
        msg = json.loads(line)
        send_url(msg)

sys.stdin.close()
