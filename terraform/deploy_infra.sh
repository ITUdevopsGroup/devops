echo Installing Docker Engine
sudo apt remove $(dpkg --get-selections docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc | cut -f1)
sudo apt update
# sudo apt install -y ca-certificates curl
# sudo install -m 0755 -d /etc/apt/keyrings
# sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
# sudo chmod a+r /etc/apt/keyrings/docker.asc
# sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
# Types: deb
# URIs: https://download.docker.com/linux/ubuntu
# Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
# Components: stable
# Architectures: $(dpkg --print-architecture)
# Signed-By: /etc/apt/keyrings/docker.asc
# EOF
#sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo dpkg -i /tmp/docker/containerd.io.deb \
  /tmp/docker/docker-ce.deb \
  /tmp/docker/docker-ce-cli.deb \
  /tmp/docker/docker-buildx-plugin.deb \
  /tmp/docker/docker-compose-plugin.deb
sudo docker compose version
echo Starting Docker Engine
sudo systemctl start docker
echo "Running docker as non-root"
sudo groupadd docker
sudo usermod -aG docker $USER
echo Logging in to Docker Hub
sudo docker login -u andersfrimann -p itudevops
echo Executing Docker Compose
cd /tmp/
sudo docker compose up
echo DONE!