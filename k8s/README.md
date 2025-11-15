# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the Banking Gen AI Application to Minikube.

## Prerequisites

- Minikube installed and running
- kubectl configured
- Docker installed
- Ollama running (can be on host machine or in cluster)

## Deployment Steps

### 1. Start Minikube
```bash
minikube start
```

### 2. Create Secrets
```bash
kubectl apply -f secrets.yaml
```

### 3. Deploy PostgreSQL Database
```bash
kubectl apply -f postgres-deployment.yaml
```

Wait for PostgreSQL to be ready:
```bash
kubectl wait --for=condition=ready pod -l app=postgres --timeout=300s
```

### 4. Build and Load Docker Image
```bash
# Set Docker environment to use Minikube's Docker daemon
eval $(minikube docker-env)

# Build the image
docker build -t banking-gen-ai-app:latest ..

# Verify image is loaded
docker images | grep banking-gen-ai-app
```

### 5. Deploy Application
```bash
kubectl apply -f deployment.yaml
```

### 6. Verify Deployment
```bash
# Check pods
kubectl get pods

# Check services
kubectl get services

# View application logs
kubectl logs -f deployment/banking-gen-ai-app
```

### 7. Access the Application

#### Option 1: Using Minikube Service
```bash
minikube service banking-gen-ai-service
```

#### Option 2: Port Forwarding
```bash
kubectl port-forward service/banking-gen-ai-service 8080:8080
```
Then access at: http://localhost:8080

#### Option 3: NodePort (if configured)
```bash
# Get minikube IP
minikube ip

# Access via NodePort (default: 30080)
curl http://$(minikube ip):30080/api/v1/health
```

## Ollama Configuration

### Option 1: Ollama on Host Machine
If Ollama is running on your host machine, update the deployment to use `host.docker.internal`:
```yaml
env:
- name: SPRING_AI_OLLAMA_BASE_URL
  value: "http://host.docker.internal:11434"
```

### Option 2: Ollama in Kubernetes
Deploy Ollama as a separate service in the cluster and reference it via service name.

## Troubleshooting

### Check Pod Status
```bash
kubectl describe pod <pod-name>
```

### View Logs
```bash
kubectl logs <pod-name>
kubectl logs -f deployment/banking-gen-ai-app
```

### Check Service Endpoints
```bash
kubectl get endpoints banking-gen-ai-service
```

### Database Connection Issues
```bash
# Check PostgreSQL pod
kubectl logs -l app=postgres

# Test database connection
kubectl exec -it <postgres-pod> -- psql -U banking_user -d bankingdb
```

### Clean Up
```bash
# Delete all resources
kubectl delete -f deployment.yaml
kubectl delete -f postgres-deployment.yaml
kubectl delete -f secrets.yaml

# Or delete everything
kubectl delete all --all
```

## Scaling

To scale the application:
```bash
kubectl scale deployment banking-gen-ai-app --replicas=3
```

## Resource Limits

Current resource limits are set in `deployment.yaml`:
- Requests: 512Mi memory, 500m CPU
- Limits: 1Gi memory, 1000m CPU

Adjust based on your cluster capacity and requirements.

