sudo rm /etc/containerd/config.toml
sudo systemctl restart containerd
sudo kubeadm init --pod-network-cidr=10.244.0.0/16
sudo chmod 755 /etc/kubernetes/admin.conf
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
kubectl apply -f https://raw.githubusercontent.com/flannel-io/flannel/master/Documentation/kube-flannel.yml
#kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml

sudo modprobe br_netfilter
sudo echo -e "\nbr_netfilter" >> /etc/modules-load.d/br_netfilter.conf
sudo echo -e "\nnet.bridge.bridge-nf-call-ip6tables=1" >> /etc/modules-load.d/br_netfilter.conf
sudo echo -e "\nnet.bridge.bridge-nf-call-iptables=1" >> /etc/modules-load.d/br_netfilter.conf
sudo echo -e "\nnet.ipv4.ip_forward=1" >> /etc/modules-load.d/br_netfilter.conf
sudo sysctl --system
sudo systemctl restart kubelet
kubectl get pods -n kube-flannel -o wide --watch




