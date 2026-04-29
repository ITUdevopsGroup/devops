terraform {
  required_providers {
    digitalocean = {
      source = "digitalocean/digitalocean"
    }
  }
}

variable "do_token" {
  type      = string
  sensitive = true
}

provider "digitalocean" {
  token = var.do_token
}

resource "digitalocean_droplet" "node" {
  name       = "MiniTwit-Database"
  region     = "fra1"
  size       = "s-2vcpu-4gb"
  image      = "ubuntu-24-04-x64"
  backups    = true
  monitoring = true
  vpc_uuid   = "b20fe126-9ab5-4cfc-9a08-98e888c6e552"
  tags       = []

  lifecycle {
    ignore_changes = [public_networking]
  }
}

data "digitalocean_ssh_key" "maria" {
  name = "Maria@pop-os"
}

resource "digitalocean_droplet" "node2" {
  name       = "MiniTwit-Worker"
  region     = "fra1"
  size       = "s-1vcpu-2gb"
  image      = "ubuntu-24-04-x64"
  backups    = true
  monitoring = true
  vpc_uuid   = "b20fe126-9ab5-4cfc-9a08-98e888c6e552"
  tags       = []
  ssh_keys   = [data.digitalocean_ssh_key.maria.fingerprint]

}




output "ip_main" {
  value = digitalocean_droplet.node.ipv4_address
}

output "ip_worker" {
  value = digitalocean_droplet.node2.ipv4_address
}