#!/usr/bin/env python3
import os
import sys
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

NOTIFY_EMAIL = os.environ.get('NOTIFY_EMAIL')
SMTP_HOST    = os.environ.get('SMTP_HOST', 'mailhog')
SMTP_PORT    = int(os.environ.get('SMTP_PORT', '1025'))
FROM_EMAIL   = os.environ.get('FROM_EMAIL', 'jenkins@saferoute.local')

JOB_NAME     = os.environ.get('JOB_NAME', 'SafeRoute')
BUILD_NUMBER = os.environ.get('BUILD_NUMBER', '0')
BUILD_URL    = os.environ.get('BUILD_URL', '')
BUILD_STATUS = os.environ.get('BUILD_STATUS', 'UNKNOWN')

if not NOTIFY_EMAIL:
    print('[notify] ERROR: NOTIFY_EMAIL environment variable is not set.')
    sys.exit(1)

subject = f'[SafeRoute CI] {BUILD_STATUS} — {JOB_NAME} #{BUILD_NUMBER}'

body = f"""\
Pipeline Execution Report
=========================
Job:    {JOB_NAME}
Build:  #{BUILD_NUMBER}
Status: {BUILD_STATUS}
URL:    {BUILD_URL if BUILD_URL else '(not available)'}

This message was generated automatically by the SafeRoute CI pipeline.
"""

msg = MIMEMultipart()
msg['From']    = FROM_EMAIL
msg['To']      = NOTIFY_EMAIL
msg['Subject'] = subject
msg.attach(MIMEText(body, 'plain'))

try:
    with smtplib.SMTP(SMTP_HOST, SMTP_PORT) as server:
        server.sendmail(FROM_EMAIL, NOTIFY_EMAIL, msg.as_string())
    print(f'[notify] Email sent to {NOTIFY_EMAIL} via {SMTP_HOST}:{SMTP_PORT}')
except Exception as e:
    print(f'[notify] ERROR sending email: {e}')
    sys.exit(1)
