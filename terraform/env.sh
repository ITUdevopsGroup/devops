#!/bin/bash
set -x
echo "Initializing environment for Terraform"
export DROPLET_SIZE="s-1vcpu-2gb" # You can select a droplet size from https://slugs.do-api.dev/
export PVT_KEY="$HOME/.ssh/xxx" #path to local private key used for digital ocean
export PUB_KEY="$HOME/.ssh/xxx.pub" #path to local public key which has been uploaded to digital ocean 
export DIGITALOCEAN_SSH_KEY="terraform"
export DO_PAT="xxx"
export TF_LOG="INFO"

# Install Terraform from https://developer.hashicorp.com/terraform/install
# Run following commands to install infra from within this folder
terraform init
terraform plan   -var "do_token=${DO_PAT}"   -var "pvt_key=${PVT_KEY}"   -var "pub_key=${PUB_KEY}"   -var "do_ssh_key_name=${DIGITALOCEAN_SSH_KEY}"   -var "droplet_size=${DROPLET_SIZE}"
terraform apply   -var "do_token=${DO_PAT}"   -var "pvt_key=${PVT_KEY}"   -var "pub_key=${PUB_KEY}"   -var "do_ssh_key_name=${DIGITALOCEAN_SSH_KEY}"   -var "droplet_size=${DROPLET_SIZE}"
# terraform destroy