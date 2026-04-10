#!/bin/bash
set -x
echo "Initializing environment for Terraform"
export DROPLET_SIZE="s-1vcpu-512mb-50gb" # You can select a droplet size from https://slugs.do-api.dev/
export PVT_KEY="$HOME/.ssh/xxx"
export PUB_KEY="$HOME/.ssh/xxx.pub"
export DIGITALOCEAN_SSH_KEY="terraform"
export DO_PAT="xxx"
export TF_LOG="INFO"

# Run following commands to install infra
# terraform init
# terraform plan   -var "do_token=${DO_PAT}"   -var "pvt_key=${PVT_KEY}"   -var "pub_key=${PUB_KEY}"   -var "do_ssh_key_name=${DIGITALOCEAN_SSH_KEY}"   -var "droplet_size=${DROPLET_SIZE}"
# terraform apply   -var "do_token=${DO_PAT}"   -var "pvt_key=${PVT_KEY}"   -var "pub_key=${PUB_KEY}"   -var "do_ssh_key_name=${DIGITALOCEAN_SSH_KEY}"   -var "droplet_size=${DROPLET_SIZE}"
