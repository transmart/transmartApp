#!/usr/bin/env python3
#
# Tested with python 3.4
#
# This script will read json-encoded auditlog messages from Transmart and send them as HTTP POST messages to
# metrics.pfizer.com. The input must be provided as json objects separated by newlines. The json objects themselves
# must not contain unencoded newlines.
#
# This script will make up to THREADS requests in parallel to the metrics server. If a request fails this is noted
# on stderr, but the request is not retried so in that case the message is lost.

import sys, os
import logging
import concurrent.futures
import json
from threading import Lock
import urllib.request
import urllib.parse
from urllib.request import Request
# httpagentparser is not in the standard library, use "pip3 install httpagentparser" to install it
import httpagentparser

URL = 'http://metrics.pfizer.com/metrics/servlet/RegisterEventServlet'
# Maximum timeout when waiting on the metrics server
TIMEOUT = 10 #seconds
# If there are more than this number of unprocessed messages, further messages will be dropped until the worker
# threads catch up.
MAX_QUEUE_LENGTH = 1000
# Max number of parallel requests to the metrics server
THREADS = 20


countlock = Lock()
queuelength = 0
failcount = 0

logging.basicConfig(level=logging.INFO, format='{asctime} {levelname}: {message}', style='{',
                    datefmt="%Y-%m-%d %H:%M:%S%z")
log = logging.getLogger()

def send_auditlog_record(line):
    global failcount, queuelength

    with countlock:
        queuelength -= 1

    # Invalid json input is not an error we can handle
    msg = json.loads(line)
    task = msg['event']
    user = msg['user']
    if task == "User Access":
        action = user
    else:
        action = msg.get('action') or '|'.join(msg.get(x) for x in ('study', 'subset1', 'subset2', 'analysis', 'query', 'facetQuery', 'clientId') if msg.get(x))
    args = dict(action = action,
                application = msg['program'],
                appVersion = msg['programVersion'],
                user = user,
                task = task,
            )
    if msg['userAgent']:
        args['browser'] = '<unknown browser>'
        browser = httpagentparser.detect(msg['userAgent']).get('browser')
        if browser:
            args['browser'] = browser['name'] + ' ' + browser['version']
    fullurl = URL + '?' + urllib.parse.urlencode(args)
    #print(fullurl)
    try:
        #raise Exception('testing')
        urllib.request.urlopen(Request(fullurl, method='POST'), timeout=TIMEOUT).readall()
    except Exception as exc:
        with countlock:
            failcount += 1
        log.error("{e}, url: {url}".format(e=' '.join(str(e) for e in exc.args), url=fullurl))

def process(line):
    try:
        send_auditlog_record(line)
    except BaseException as e:
        # An exception here is a programming error or something serious. As this is not the main thread we can't just
        # let the exception bubble up, so kill ourselves forcefully.
        log.fatal(str(e) + ", aborting!", exc_info=e)
        os.abort()


with concurrent.futures.ThreadPoolExecutor(max_workers=THREADS) as executor:
    for line in sys.stdin:
        if queuelength > MAX_QUEUE_LENGTH:
            log.error("MAX_QUEUE_LENGTH exceeded, ignoring message {line}".format(line=line))
            continue
        with countlock:
            queuelength += 1
        executor.submit(process, line)

sys.stdin.close()
