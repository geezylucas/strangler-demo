# Strangler Pattern Demo - Java Spring Boot en Azure Container Apps

Este proyecto demuestra el **patrón Strangler** para modernizar aplicaciones legacy mediante la extracción gradual de funcionalidad. Utiliza Spring Boot 3.5, Java 21, GitHub Actions para CI/CD, y Azure Container Apps para el deployment.

## 📋 Tabla de Contenidos

- [¿Qué es el Patrón Strangler?](#qué-es-el-patrón-strangler)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
- [Prerrequisitos](#prerrequisitos)
- [Configuración Local](#configuración-local)
- [Deployment en Azure](#deployment-en-azure)
- [Configuración de CI/CD con GitHub Actions](#configuración-de-cicd-con-github-actions)
- [Testing de la API](#testing-de-la-api)
- [Siguientes Pasos](#siguientes-pasos)

---

## 🎯 ¿Qué es el Patrón Strangler?

El **Patrón Strangler** (Strangler Fig Pattern) es una estrategia de migración incremental para modernizar sistemas legacy sin necesidad de una reescritura completa de big bang.

### Concepto

Como la planta strangler fig que crece alrededor de un árbol existente:
1. Se crea una nueva aplicación que actúa como fachada
2. Gradualmente se interceptan llamadas al sistema legacy
3. La nueva funcionalidad se implementa en el sistema nuevo
4. Poco a poco se reemplaza el sistema legacy
5. Finalmente, el sistema legacy se retira completamente

### Ventajas

✅ Menor riesgo que una reescritura completa  
✅ Entrega continua de valor durante la migración  
✅ Permite aprender y ajustar el enfoque  
✅ El sistema legacy sigue funcionando mientras migramos  
✅ Podemos agregar nuevas funcionalidades sin tocar el código legacy

### En Este Proyecto

Simulamos que estamos extrayendo la **funcionalidad de gestión de usuarios** de un sistema legacy (representado por jsonplaceholder.typicode.com):

- **Fase Actual**: Nuestra API actúa como proxy al sistema legacy
- **Nueva Funcionalidad**: Agregamos filtrado por ciudad (no disponible en legacy)
- **Futuro**: Podríamos agregar caché, migrar datos a nuestra BD, y eventualmente desconectar el legacy

---

## 🏗️ Arquitectura del Proyecto

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │
       │ HTTP Request
       ▼
┌─────────────────────────────┐
│  Nueva API (Spring Boot)    │
│  /api/v1/users/*            │
│                             │
│  ┌─────────────────────┐   │
│  │  UserController     │   │
│  └──────────┬──────────┘   │
│             │               │
│  ┌──────────▼──────────┐   │
│  │  UserService        │   │◄─── Aquí es donde se
│  │  (Strangler Logic)  │   │     implementa la lógica
│  └──────────┬──────────┘   │     del patrón Strangler
│             │               │
│  ┌──────────▼──────────┐   │
│  │  LegacyApiClient    │   │
│  └──────────┬──────────┘   │
└─────────────┼───────────────┘
              │
              │ HTTP Request (Proxy)
              ▼
┌─────────────────────────────┐
│  Sistema Legacy             │
│  (jsonplaceholder API)      │
│  /users/*                   │
└─────────────────────────────┘
```

### Componentes

- **UserController**: Expone la nueva API REST
- **UserService**: Implementa la lógica del patrón Strangler
- **LegacyApiClient**: Se comunica con el sistema legacy
- **Models**: DTOs para transferencia de datos

---

## 🛠️ Tecnologías Utilizadas

- **Java 21** - Última versión LTS
- **Spring Boot 3.5.0** - Framework web
- **Spring WebFlux** - Para consumir APIs externas (WebClient)
- **Maven** - Gestión de dependencias
- **Docker** - Containerización
- **Azure Container Apps** - Plataforma de deployment
- **GitHub Actions** - CI/CD pipeline

---

## 📦 Prerrequisitos

Antes de comenzar, asegúrate de tener instalado:

- [Java 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [Docker Desktop](https://www.docker.com/products/docker-desktop)
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
- [Git](https://git-scm.com/)
- Una cuenta de [Azure](https://azure.microsoft.com/free/) (con créditos gratuitos)
- Una cuenta de [GitHub](https://github.com/)

### Verificar instalaciones

```bash
java -version        # Debe mostrar Java 21
mvn -version         # Debe mostrar Maven 3.9+
docker --version     # Debe mostrar Docker
az --version         # Debe mostrar Azure CLI
```

---

## 💻 Configuración Local

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/strangler-demo.git
cd strangler-demo
```

### 2. Ejecutar Localmente (Sin Docker)

```bash
# Compilar el proyecto
mvn clean package

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

### 3. Probar la API Localmente

```bash
# Obtener todos los usuarios
curl http://localhost:8080/api/v1/users

# Obtener un usuario específico
curl http://localhost:8080/api/v1/users/1

# Nueva funcionalidad: Filtrar por ciudad
curl http://localhost:8080/api/v1/users/city/Gwenborough

# Health check
curl http://localhost:8080/actuator/health
```

### 4. Ejecutar con Docker Localmente

```bash
# Build de la imagen
docker build -t strangler-demo:local .

# Ejecutar el contenedor
docker run -p 8080:8080 strangler-demo:local

# Probar
curl http://localhost:8080/api/v1/users
```

---

## ☁️ Deployment en Azure

### Paso 1: Login en Azure

```bash
az login
```

Esto abrirá tu navegador para autenticarte.

### Paso 2: Configurar Variables

```bash
# Exportar variables (cambia los valores según tu preferencia)
export RESOURCE_GROUP="strangler-demo-rg"
export LOCATION="eastus"
export ACR_NAME="stranglerdemo$(openssl rand -hex 4)"  # Nombre único
export CONTAINER_APP_ENV="strangler-demo-env"
export CONTAINER_APP_NAME="strangler-demo-app"
```

### Paso 3: Crear Resource Group

```bash
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
```

### Paso 4: Crear Azure Container Registry (ACR)

```bash
# Crear el registry
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic \
  --admin-enabled true

# Obtener credenciales
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)

echo "ACR Username: $ACR_USERNAME"
echo "ACR Password: $ACR_PASSWORD"
```

> 💡 **Guarda estas credenciales**, las necesitarás para GitHub Actions.

### Paso 5: Build y Push de la Imagen a ACR

```bash
# Login en ACR
az acr login --name $ACR_NAME

# Build y push usando ACR
az acr build \
  --registry $ACR_NAME \
  --image strangler-demo:latest \
  --file Dockerfile \
  .
```

### Paso 6: Crear Container Apps Environment

```bash
az containerapp env create \
  --name $CONTAINER_APP_ENV \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION
```

### Paso 7: Desplegar la Container App

```bash
az containerapp create \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image $ACR_NAME.azurecr.io/strangler-demo:latest \
  --registry-server $ACR_NAME.azurecr.io \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8080 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 5 \
  --cpu 0.5 \
  --memory 1.0Gi
```

### Paso 8: Obtener la URL de la Aplicación

```bash
APP_URL=$(az containerapp show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "Tu aplicación está disponible en: https://$APP_URL"
```

### Paso 9: Probar la Aplicación en Azure

```bash
# Obtener todos los usuarios
curl https://$APP_URL/api/v1/users

# Obtener usuario por ID
curl https://$APP_URL/api/v1/users/1

# Filtrar por ciudad (nueva funcionalidad)
curl https://$APP_URL/api/v1/users/city/Gwenborough

# Health check
curl https://$APP_URL/actuator/health
```

---

## 🚀 Configuración de CI/CD con GitHub Actions

### Paso 1: Fork/Push del Repositorio

```bash
# Si aún no lo has hecho, sube tu código a GitHub
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/tu-usuario/strangler-demo.git
git push -u origin main
```

### Paso 2: Crear Service Principal para GitHub Actions

```bash
# Obtener tu subscription ID
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

# Crear service principal
az ad sp create-for-rbac \
  --name "strangler-demo-github-actions" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP \
  --sdk-auth
```

Este comando te devolverá un JSON. **Copia todo el output**.

### Paso 3: Configurar GitHub Secrets

Ve a tu repositorio en GitHub → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Crea los siguientes secrets:

1. **AZURE_CREDENTIALS**
   - Pega el JSON completo del service principal

2. **ACR_USERNAME**
   - Pega el username del ACR (lo obtuviste en el Paso 4 de Deployment)

3. **ACR_PASSWORD**
   - Pega el password del ACR

### Paso 4: Actualizar Workflow Variables

Edita el archivo `.github/workflows/azure-container-apps.yml` y actualiza estas variables:

```yaml
env:
  AZURE_CONTAINER_REGISTRY: <tu-acr-name>  # Sin .azurecr.io
  RESOURCE_GROUP: strangler-demo-rg
  CONTAINER_APP_NAME: strangler-demo-app
  LOCATION: eastus
```

### Paso 5: Hacer Push y Ver el Deploy Automático

```bash
git add .github/workflows/azure-container-apps.yml
git commit -m "Configure CI/CD pipeline"
git push
```

Ve a tu repositorio → **Actions** para ver el workflow ejecutándose.

### Pipeline Explicado

El workflow hace lo siguiente:

1. **Build and Test**: Compila y ejecuta tests
2. **Build and Push Image**: Construye la imagen Docker y la sube a ACR
3. **Deploy to Azure**: Despliega la nueva versión en Azure Container Apps

Cada push a `main` dispara el deployment automático. 🎉

---

## 🧪 Testing de la API

### Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/users` | Obtener todos los usuarios |
| GET | `/api/v1/users/{id}` | Obtener usuario por ID |
| GET | `/api/v1/users/city/{city}` | Filtrar usuarios por ciudad (nueva funcionalidad) |
| GET | `/api/v1/users/health` | Health check del servicio |
| GET | `/actuator/health` | Health check de Spring Boot |

### Ejemplos de Request/Response

**GET /api/v1/users**
```json
[
  {
    "id": 1,
    "name": "Leanne Graham",
    "username": "Bret",
    "email": "Sincere@april.biz",
    "address": {
      "street": "Kulas Light",
      "suite": "Apt. 556",
      "city": "Gwenborough",
      "zipcode": "92998-3874",
      "geo": {
        "lat": "-37.3159",
        "lng": "81.1496"
      }
    },
    "phone": "1-770-736-8031 x56442",
    "website": "hildegard.org",
    "company": {
      "name": "Romaguera-Crona",
      "catchPhrase": "Multi-layered client-server neural-net",
      "bs": "harness real-time e-markets"
    }
  }
  // ... más usuarios
]
```

**GET /api/v1/users/city/Gwenborough**
```json
[
  {
    "id": 1,
    "name": "Leanne Graham",
    // ... usuario de Gwenborough
  }
]
```

---

## 📈 Monitoreo y Logs

### Ver logs en tiempo real

```bash
az containerapp logs show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --follow
```

### Ver métricas en Azure Portal

1. Ve a [Azure Portal](https://portal.azure.com)
2. Busca tu Container App
3. En el menú lateral: **Monitoring** → **Metrics**
4. Métricas disponibles: Requests, Response Time, CPU, Memory

### Application Insights (Opcional)

Para observabilidad avanzada, puedes integrar Application Insights. Ver documentación oficial.

---

## 🔄 Siguientes Pasos en la Migración

Este proyecto demuestra la **Fase 1** del patrón Strangler. Aquí están las siguientes fases sugeridas:

### Fase 2: Agregar Caché
```java
@Cacheable("users")
public List<User> getAllUsers() {
    return legacyApiClient.getAllUsers();
}
```

### Fase 3: Agregar Base de Datos Nueva
- Implementar Azure SQL Database o Cosmos DB
- Comenzar a sincronizar datos del legacy
- Implementar dual-write (escribir en ambos sistemas)

### Fase 4: Implementar Feature Toggles
```java
if (featureToggle.useNewDatabase()) {
    return newUserRepository.findAll();
} else {
    return legacyApiClient.getAllUsers();
}
```

### Fase 5: Migración Gradual de Reads
- Comenzar a leer del nuevo sistema
- Mantener legacy como fallback
- Monitorear comportamiento

### Fase 6: Migración de Writes
- Escribir en el nuevo sistema
- Sincronizar con legacy (temporalmente)
- Validar integridad de datos

### Fase 7: Retiro del Sistema Legacy
- Desconectar completamente el legacy
- Eliminar código de proxy
- Celebrar 🎉

---

## 🛡️ Mejores Prácticas Implementadas

✅ **Containerización**: Aplicación completamente containerizada  
✅ **Multi-stage build**: Optimización de tamaño de imagen  
✅ **Non-root user**: Seguridad en el contenedor  
✅ **Health checks**: Probes de Kubernetes incluidos  
✅ **Externalized config**: Configuración mediante variables de entorno  
✅ **Observability**: Actuator endpoints para monitoreo  
✅ **CI/CD**: Pipeline automatizado  
✅ **Scaling**: Auto-scaling configurado (1-5 réplicas)

---

## 📚 Recursos Adicionales

- [Patrón Strangler - Martin Fowler](https://martinfowler.com/bliki/StranglerFigApplication.html)
- [Azure Container Apps Documentation](https://docs.microsoft.com/en-us/azure/container-apps/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

---

## 🐛 Troubleshooting

### La aplicación no inicia localmente

```bash
# Verifica la versión de Java
java -version  # Debe ser 21

# Limpia y recompila
mvn clean install
```

### Error al conectar con ACR

```bash
# Relogin en ACR
az acr login --name $ACR_NAME

# Verifica que admin está habilitado
az acr update --name $ACR_NAME --admin-enabled true
```

### Container App no responde

```bash
# Ver logs
az containerapp logs show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --tail 100

# Verificar estado
az containerapp show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query "properties.runningStatus"
```

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📝 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

## 👨‍💻 Autor

Tu Nombre - [@geezy_lucas](https://x.com/geezy_lucas)

Para preguntas o feedback, abre un issue en GitHub.

---

**¡Happy Coding!** 🚀
