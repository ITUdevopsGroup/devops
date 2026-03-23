kubectl taint nodes ip-172-31-35-116 dedicated=special-user:NoSchedule-
kubectl taint nodes ip-172-31-45-88 dedicated=special-user:NoSchedule-

sudo echo -e "\nnodes: 
  - role: control-plane 
    extraPortMappings: 
      - containerPort: 31437 
        hostPort: 5001 \
        protocol: TCP \
      - containerPort: 31438 
        hostPort: 3000 \
        protocol: TCP \
      - containerPort: 30478 
        hostPort: 8443 
        protocol: TCP" >> /home/ubuntu/.kube/config"

kubectl kustomize "https://github.com/nginx/nginx-gateway-fabric/config/crd/gateway-api/standard?ref=v2.4.2" | kubectl apply -f -

helm repo add jetstack https://charts.jetstack.io
helm repo update

helm install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set config.apiVersion="controller.config.cert-manager.io/v1alpha1" \
  --set config.kind="ControllerConfiguration" \
  --set config.enableGatewayAPI=true \
  --set crds.enabled=true


helm upgrade -i ngf oci://ghcr.io/nginx/charts/nginx-gateway-fabric --create-namespace -n nginx-gateway --set nginx.service.type=NodePort --set-json 'nginx.service.nodePorts=[{"port":31437,"listenerPort":5002},{"port":31438,"listenerPort":3002}, {"port":30478,"listenerPort":8443}]'

cat <<EOF > gateway.yaml
apiVersion: gateway.networking.k8s.io/v1
kind: Gateway
metadata:
  name: gateway
spec:
  gatewayClassName: nginx
  listeners:
  - name: devops-backend
    port: 5002
    protocol: HTTP
    hostname: "ec2-13-48-56-247.eu-north-1.compute.amazonaws.com"
  - name: devops-frontend
    port: 3002
    protocol: HTTP
    hostname: "ec2-13-48-56-247.eu-north-1.compute.amazonaws.com"
EOF

kubectl apply -f gateway.yaml
