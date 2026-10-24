#!/bin/bash
echo ">>> running create-bucket.sh"
awslocal s3 mb s3://oms-documents
awslocal s3 mb s3://oms-reports