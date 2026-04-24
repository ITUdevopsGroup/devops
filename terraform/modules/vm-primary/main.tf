resource "digitalocean_firewall" "vm-primary" {
  name = "devops"

    droplet_ids = [digitalocean_droplet.vm-primary.id]

      inbound_rule {
      protocol         = "tcp"
      port_range       = "5001"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

      inbound_rule {
      protocol         = "tcp"
      port_range       = "9090"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

      inbound_rule {
      protocol         = "tcp"
      port_range       = "3100"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

      inbound_rule {
      protocol         = "tcp"
      port_range       = "5432"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

      inbound_rule {
      protocol         = "tcp"
      port_range       = "3000"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

      inbound_rule {
      protocol         = "tcp"
      port_range       = "3001"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

      inbound_rule {
      protocol         = "tcp"
      port_range       = "22"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

    inbound_rule {
      protocol         = "tcp"
      port_range       = "80"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

    inbound_rule {
      protocol         = "tcp"
      port_range       = "443"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

    inbound_rule {
      protocol         = "icmp"
      source_addresses = ["0.0.0.0/0", "::/0"]
    }

    outbound_rule {
      protocol              = "tcp"
      port_range            = "53"
      destination_addresses = ["0.0.0.0/0", "::/0"]
    }

    outbound_rule {
      protocol              = "udp"
      port_range            = "53"
      destination_addresses = ["0.0.0.0/0", "::/0"]
    }

    outbound_rule {
      protocol              = "icmp"
      destination_addresses = ["0.0.0.0/0", "::/0"]
    }
  }

resource "digitalocean_droplet" "vm-primary" {
  image  = var.ubuntu_image
  name   = var.droplet_name
  region = var.deployment_region
  size   = var.droplet_size
  ssh_keys = [
    data.digitalocean_ssh_key.set_ssh_key.id
  ]

 connection {
    host = self.ipv4_address
    user = "root"
    type = "ssh"
    private_key = file(var.pvt_key)
    timeout = "4m"
  }

  provisioner "file" {
    source = "C:/Users/ander/devops/devops/docker-compose.yml"
    destination = "/tmp/docker-compose.yml"
  }

  provisioner "file" {
    source = "C:/Users/ander/devops/devops/terraform/docker.zip"
    destination = "/tmp/docker.zip"
  }

  provisioner "file" {
    source = "C:/Users/ander/devops/devops/terraform/deploy_infra.sh"
    destination = "/tmp/deploy_infra.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "export PATH=$PATH:/usr/bin",
      "sudo apt install -y zip",
      "chmod 755 /tmp/deploy_infra.sh",
      "chmod 755 /tmp/docker.zip",
      "cd /tmp/",
      "unzip docker.zip",
      "chmod -R 755 /tmp/docker",
      "./deploy_infra.sh"
    ]
  }
}

