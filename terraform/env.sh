#!/bin/bash
set -x
echo "Initializing environment for Terraform"
export DROPLET_SIZE=${DROPLET_SIZE:-"s-1vcpu-512mb-50gb"} # You can select a droplet size from https://slugs.do-api.dev/
export PVT_KEY="$HOME/.ssh/id_rsa"
export PUB_KEY="$HOME/.ssh/id_rsa.pub"
export DIGITALOCEAN_SSH_KEY="terraform"
export DO_PAT="dop_v1_d3d0baae25e8873ae77f03aefc602f9471d3a071cabea4637fbd1b501d436c13"
export TF_LOG="INFO"