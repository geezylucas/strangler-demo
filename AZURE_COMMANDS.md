# Comandos Útiles para Azure Container Apps

## Variables de configuración
```bash
RESOURCE_GROUP="strangler-demo-rg"
CONTAINER_APP_NAME="strangler-demo-app"
ACR_NAME="<your-acr-name>"
```

## Monitoreo y Debugging

### Ver logs en tiempo real
```bash
az containerapp logs show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --follow
```

### Ver últimos logs
```bash
az containerapp logs show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --tail 100
```

### Ver métricas de la aplicación
```bash
az monitor metrics list \
  --resource "/subscriptions/{subscription-id}/resourceGroups/$RESOURCE_GROUP/providers/Microsoft.App/containerApps/$CONTAINER_APP_NAME" \
  --metric "Requests"
```

## Gestión de Réplicas

### Escalar manualmente
```bash
az containerapp update \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 2 \
  --max-replicas 10
```

### Ver réplicas activas
```bash
az containerapp replica list \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP
```

## Actualización de la Aplicación

### Deploy de nueva imagen
```bash
az containerapp update \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --image $ACR_NAME.azurecr.io/strangler-demo:latest
```

### Actualizar variables de entorno
```bash
az containerapp update \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" "CUSTOM_VAR=value"
```

## Revisiones y Rollback

### Listar revisiones
```bash
az containerapp revision list \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --output table
```

### Activar una revisión específica (rollback)
```bash
az containerapp revision activate \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --revision <revision-name>
```

### Desactivar una revisión
```bash
az containerapp revision deactivate \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --revision <revision-name>
```

## Testing

### Test local con Docker
```bash
# Build
docker build -t strangler-demo:local .

# Run
docker run -p 8080:8080 strangler-demo:local

# Test
curl http://localhost:8080/api/v1/users
```

### Test en Azure
```bash
APP_URL=$(az containerapp show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

curl https://$APP_URL/api/v1/users
curl https://$APP_URL/api/v1/users/1
curl https://$APP_URL/api/v1/users/city/Gwenborough
curl https://$APP_URL/actuator/health
```

## Limpieza de Recursos

### Eliminar solo la Container App
```bash
az containerapp delete \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --yes
```

### Eliminar todo el Resource Group
```bash
az group delete \
  --name $RESOURCE_GROUP \
  --yes
```

## Troubleshooting

### Ver estado de la aplicación
```bash
az containerapp show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query "properties.{provisioningState:provisioningState,runningStatus:runningStatus}" \
  --output table
```

### Ver eventos
```bash
az containerapp revision list \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query "[].{Name:name,CreatedTime:properties.createdTime,Active:properties.active,Replicas:properties.replicas}" \
  --output table
```

### Obtener URL de ingress
```bash
az containerapp show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn \
  --output tsv
```
