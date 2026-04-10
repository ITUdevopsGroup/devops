terraform {
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = "~> 2.0"
    }
  }
}

provider "digitalocean" {
  alias = "digitalocean"
  token = var.do_token
}


module "run_vm_primary" {
  source = "./modules/vm-primary"
  providers = {
    digitalocean = digitalocean.digitalocean
  }
  do_token          = var.do_token
  pvt_key           = var.pvt_key
  pub_key           = var.pub_key
  do_ssh_key_name   = var.do_ssh_key_name
  ubuntu_image      = var.ubuntu_image
  droplet_name      = var.droplet_name
  deployment_region = var.deployment_region
  droplet_size      = var.droplet_size
}

output "droplet_ip_address" {
  value     = module.run_vm_primary.droplet_ip_address
  sensitive = false # set to true if the logs will be publicly available
}

variable "do_token" { sensitive = true }
variable "pvt_key" { sensitive = true }
variable "pub_key" {}
variable "do_ssh_key_name" {}
variable "ubuntu_image" { default = "ubuntu-24-04-x64" }
variable "droplet_name" { default = "vm-primary" }
variable "deployment_region" { default = "fra1" }
variable "droplet_size" { default = "s-4vcpu-8gb" }