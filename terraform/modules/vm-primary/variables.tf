locals {
  connection_user = "root"
}
variable "do_token" { type = string }
variable "pvt_key" { type = string }
variable "pub_key" { type = string }
variable "do_ssh_key_name" { type = string }
variable "ubuntu_image" { type = string }
variable "deployment_region" { type = string }
variable "droplet_size" { type = string }
variable "droplet_name" { type = string }
