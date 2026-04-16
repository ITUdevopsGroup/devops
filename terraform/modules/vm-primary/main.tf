resource "digitalocean_droplet" "vm-primary" {
  image  = var.ubuntu_image
  name   = var.droplet_name
  region = var.deployment_region
  size   = var.droplet_size
  ssh_keys = [
    data.digitalocean_ssh_key.set_ssh_key.id
  ]

  provisioner "file" {
    source = "../../../docker-compose.yml"
    destination = "/tmp/docker-compose.yml"
  }

  provisioner "remote-exec" {
    inline = [
      "export PATH=$PATH:/usr/bin",
      "sudo apt update",

      "echo Installing Docker Engine",

      "sudo apt update",
      "sudo apt install -y ca-certificates curl",
      "sudo install -m 0755 -d /etc/apt/keyrings",
      "sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc",
      "sudo chmod a+r /etc/apt/keyrings/docker.asc",

      "sudo tee /etc/apt/sources.list.d/docker.sources <<EOF Types: deb URIs: https://download.docker.com/linux/ubuntu Suites: $$(. /etc/os-release && echo '$${UBUNTU_CODENAME:-$VERSION_CODENAME}') Components: stable Signed-By: /etc/apt/keyrings/docker.asc EOF",

      "sudo apt update",
      "sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",

      "sudo systemctl status docker",
      "sudo systemctl start docker",
      "sudo docker run hello-world",
      "sudo groupadd docker",
      "sudo usermod -aG docker $USER",
      "newgrp docker",
      "docker run hello-world",

      "echo Installing postgres"
      "sudo apt install postgresql postgresql-contrib"
      "psql --version"
      "sudo systemctl status postgresql"
      
      "echo Deploying database, minitwit and associated applications"
      "docker login -u andersfrimann -p itudevops",
      "cd /tmp/",
      "docker compose up"  

      "echo DONE!"
    ]

    connection {
      host        = self.ipv4_address
      user        = local.connection_user
      type        = "ssh"
      private_key = file(var.pvt_key)
      timeout     = "2m"
      agent       = false
    }
  }
}

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
      source_addresses = ["192.168.1.0/24", "2002:1:2::/48"]
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
