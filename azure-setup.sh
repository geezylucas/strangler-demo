#!/bin/bash

# Azure Container Apps Setup Script
# This script creates all necessary Azure resources for deploying the Strangler Demo app

# Configuration
RESOURCE_GROUP="strangler-demo-rg"
LOCATION="eastus"
ACR_NAME="stranglerdemo$(openssl rand -hex 4)"  # Random suffix to ensure uniqueness
CONTAINER_APP_ENV="strangler-demo-env"
CONTAINER_APP_NAME="strangler-demo-app"
IMAGE_NAME="strangler-demo"

echo "=========================================="
echo "Azure Container Apps Setup"
echo "=========================================="
echo "Resource Group: $RESOURCE_GROUP"
echo "Location: $LOCATION"
echo "ACR Name: $ACR_NAME"
echo "Container App Environment: $CONTAINER_APP_ENV"
echo "Container App Name: $CONTAINER_APP_NAME"
echo "=========================================="

# 1. Create Resource Group
echo "Creating resource group..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION

# 2. Create Azure Container Registry
echo "Creating Azure Container Registry..."
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic \
  --admin-enabled true


# 3. Get ACR credentials
echo "Getting ACR credentials..."
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)

echo "ACR Username: $ACR_USERNAME"
echo "ACR Password: $ACR_PASSWORD"

# 4. Create Container Apps Environment
echo "Creating Container Apps environment..."
az containerapp env create \
  --name $CONTAINER_APP_ENV \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION

# 5. Build and push initial Docker image (optional - can be done via GitHub Actions)
echo "Building and pushing Docker image..."
az acr build \
  --registry $ACR_NAME \
  --image $IMAGE_NAME:latest \
  --file Dockerfile \
  .

# 6. Create Container App
echo "Creating Container App..."
az containerapp create \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image $ACR_NAME.azurecr.io/$IMAGE_NAME:latest \
  --registry-server $ACR_NAME.azurecr.io \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8080 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 5 \
  --cpu 0.5 \
  --memory 1.0Gi \
  --env-vars "SPRING_PROFILES_ACTIVE=prod"

# 7. Get the application URL
echo "Getting application URL..."
APP_URL=$(az containerapp show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo ""
echo "=========================================="
echo "Deployment Complete!"
echo "=========================================="
echo "Application URL: https://$APP_URL"
echo "Test the API:"
echo "  curl https://$APP_URL/api/v1/users"
echo "  curl https://$APP_URL/api/v1/users/1"
echo "  curl https://$APP_URL/api/v1/users/city/Gwenborough"
echo ""
echo "ACR Details for GitHub Secrets:"
echo "  ACR_USERNAME: $ACR_USERNAME"
echo "  ACR_PASSWORD: $ACR_PASSWORD"
echo "  AZURE_CONTAINER_REGISTRY: $ACR_NAME"
echo "=========================================="

# 8. Create service principal for GitHub Actions (optional)
echo ""
echo "Creating service principal for GitHub Actions..."
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

SP_OUTPUT=$(az ad sp create-for-rbac \
  --name "strangler-demo-github-actions" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP \
  --sdk-auth)

echo ""
echo "=========================================="
echo "GitHub Secrets Configuration"
echo "=========================================="
echo "Add these secrets to your GitHub repository:"
echo ""
echo "AZURE_CREDENTIALS:"
echo "$SP_OUTPUT"
echo ""
echo "ACR_USERNAME: $ACR_USERNAME"
echo "ACR_PASSWORD: $ACR_PASSWORD"
echo "=========================================="
