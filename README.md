# SafeRoute

SafeRoute e uma arquitetura de microsservicos para recebimento de telemetria de caminhoes refrigerados, processamento dos dados e geracao de alertas quando a temperatura sai da faixa segura.

O projeto usa Spring Boot, Spring Cloud Config, Eureka, API Gateway, PostgreSQL, Jenkins, Prometheus, Grafana e manifests Kubernetes.

Validacoes executadas localmente:

```bash
docker compose config
docker compose pull
cd services/sensor-service && mvn verify
cd services/alert-service && mvn verify
docker manifest inspect thuliott/saferoute-config-server:latest
docker manifest inspect thuliott/saferoute-eureka-server:latest
docker manifest inspect thuliott/saferoute-api-gateway:latest
docker manifest inspect thuliott/saferoute-sensor-service:latest
docker manifest inspect thuliott/saferoute-alert-service:latest
docker manifest inspect thuliott/saferoute-jenkins:latest
docker manifest inspect thuliott/saferoute-simulator:latest
```

## Imagens Docker

Imagens publicadas no Docker Hub para `linux/amd64`:


| Componente       | Imagem                                                                                                   |
| ---------------- | -------------------------------------------------------------------------------------------------------- |
| `config-server`  | [`thuliott/saferoute-config-server:latest`](https://hub.docker.com/r/thuliott/saferoute-config-server)   |
| `eureka-server`  | [`thuliott/saferoute-eureka-server:latest`](https://hub.docker.com/r/thuliott/saferoute-eureka-server)   |
| `api-gateway`    | [`thuliott/saferoute-api-gateway:latest`](https://hub.docker.com/r/thuliott/saferoute-api-gateway)       |
| `sensor-service` | [`thuliott/saferoute-sensor-service:latest`](https://hub.docker.com/r/thuliott/saferoute-sensor-service) |
| `alert-service`  | [`thuliott/saferoute-alert-service:latest`](https://hub.docker.com/r/thuliott/saferoute-alert-service)   |
| `jenkins`        | [`thuliott/saferoute-jenkins:latest`](https://hub.docker.com/r/thuliott/saferoute-jenkins)               |
| `simulator`      | [`thuliott/saferoute-simulator:latest`](https://hub.docker.com/r/thuliott/saferoute-simulator)           |


## Arquitetura

Principais componentes:


| Componente       | Porta          | Descricao                                      |
| ---------------- | -------------- | ---------------------------------------------- |
| `config-server`  | `8888`         | Centraliza as configuracoes dos microsservicos |
| `eureka-server`  | `8761`         | Service discovery dos servicos Spring          |
| `api-gateway`    | `8080`         | Entrada HTTP principal da aplicacao            |
| `sensor-service` | `8081`         | Recebe telemetria dos caminhoes                |
| `alert-service`  | `8082`         | Analisa telemetria e registra alertas          |
| `postgres-db`    | `5433 -> 5432` | Banco usado pelo `alert-service`               |
| `jenkins`        | `8090`         | Pipeline CI local                              |
| `mailhog`        | `8025`, `1025` | Captura e-mails enviados pelo Jenkins          |
| `prometheus`     | `9090`         | Coleta metricas dos servicos                   |
| `grafana`        | `3000`         | Visualizacao de metricas                       |


Fluxo principal:

1. O cliente envia telemetria para o API Gateway.
2. O gateway encaminha a requisicao para o `sensor-service`.
3. O `sensor-service` envia os dados para o `alert-service` via Feign/Eureka.
4. O `alert-service` verifica a temperatura e usa PostgreSQL para persistencia.
5. Prometheus coleta metricas dos servicos.

## Requisitos

- Docker
- Docker Compose
- Git
- Java 21 e Maven, caso queira rodar os servicos fora do Docker
- Kubernetes e `kubectl`, apenas para usar a pasta `k8s`

Observacao: os servicos Java foram configurados para Java 21. Para validar testes e cobertura fora do Docker/Jenkins, use JDK 21.

## Variaveis De Ambiente

Crie um arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

Exemplo:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=senha
POSTGRES_DB=alert-service

NOTIFY_EMAIL=seu-email@exemplo.com
JENKINS_ADMIN_ID=admin
JENKINS_ADMIN_PASSWORD=admin
REPO_URL=https://github.com/FelipeRibeiro12/S07-SafeRoute
```

## Subindo Com Docker Compose

Na raiz do projeto:

```bash
docker compose pull
docker compose up -d
```

Servicos principais:

- API Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`
- Jenkins: `http://localhost:8090`
- MailHog: `http://localhost:8025`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Para parar:

```bash
docker compose down
```

Para parar removendo volumes locais:

```bash
docker compose down -v
```

## API

Envio de telemetria pelo gateway:

```bash
curl -X POST http://localhost:8080/sensor/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "truckId": "TRUCK-1",
    "temperature": 4.5,
    "latitude": -23.55,
    "longitude": -46.63,
    "timestamp": "2026-06-15T10:00:00"
  }'
```

O gateway remove o prefixo `/sensor` e encaminha para o endpoint `/telemetry` do `sensor-service`.

O `alert-service` considera a faixa segura configurada em:

```yaml
saferoute:
  temp-min: -2.0
  temp-max: 8.0
```

Valores abaixo de `-2.0` ou acima de `8.0` geram alerta.

## Simulador De Carga

O arquivo `simulator.py` envia telemetrias em massa para:

```text
http://localhost:8080/sensor/telemetry
```

Instale a dependencia e execute:

```bash
pip install aiohttp
python3 simulator.py
```

Interrompa com `Ctrl+C`.

## Testes E Build Local

Para testar os servicos com testes configurados:

```bash
cd services/sensor-service
mvn verify
```

```bash
cd services/alert-service
mvn verify
```

Para empacotar um servico:

```bash
mvn clean package -DskipTests
```

## Jenkins

O Jenkins e configurado como codigo pela pasta `jenkins/`.

Arquivos principais:

- `jenkins/Dockerfile`: cria a imagem Jenkins com Maven, Python e plugins.
- `jenkins/plugins.txt`: lista de plugins instalados.
- `jenkins/casc.yml`: configura usuario admin e cria o job `saferoute-pipeline`.
- `Jenkinsfile`: define o pipeline CI.
- `jenkins/scripts/notify_email.py`: envia e-mail ao final do build.

Pipeline:

1. Roda `mvn verify` em `sensor-service` e `alert-service`.
2. Gera relatorios JUnit.
3. Executa build dos servicos Java.
4. Organiza `.jar`, relatorios de teste e relatorios JaCoCo em `ci-artifacts/`.
5. Arquiva `ci-artifacts/**` como artefatos do build no Jenkins.
6. Envia notificacao por e-mail usando MailHog.

Depois de executar um build, os artefatos ficam disponiveis em:

```text
saferoute-pipeline -> build executado -> Build Artifacts
```

O MailHog pode ser acessado em:

```text
http://localhost:8025
```

## Observabilidade

O Prometheus esta configurado em `prometheus.yml` para coletar:

- `sensor-service:8081/actuator/prometheus`
- `alert-service:8082/actuator/prometheus`

Os endpoints expostos pelos servicos incluem:

```text
/actuator/health
/actuator/prometheus
```

Grafana:

```text
http://localhost:3000
```

Senha padrao configurada no Compose:

```text
admin
```

## Kubernetes

A pasta `k8s/` contem manifests para executar a aplicacao em Kubernetes:

- `1-postgres-db.yaml`
- `2-sensor-service.yaml`
- `3-hpa-sensor.yaml`
- `4-config-server.yaml`
- `5-eureka-server.yaml`
- `6-api-gateway.yaml`
- `7-alert-service.yaml`
- `8-hpa-gateway.yaml`
- `9-hpa-alert.yaml`

Antes de aplicar os manifests, crie o secret usado pelo PostgreSQL:

```bash
kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_USER=postgres \
  --from-literal=POSTGRES_PASSWORD=senha \
  --from-literal=POSTGRES_DB=alert-service
```

Depois aplique os manifests:

```bash
kubectl apply -f k8s/1-postgres-db.yaml
kubectl apply -f k8s/4-config-server.yaml
kubectl apply -f k8s/5-eureka-server.yaml
kubectl apply -f k8s/6-api-gateway.yaml
kubectl apply -f k8s/2-sensor-service.yaml
kubectl apply -f k8s/7-alert-service.yaml
kubectl apply -f k8s/3-hpa-sensor.yaml
kubectl apply -f k8s/8-hpa-gateway.yaml
kubectl apply -f k8s/9-hpa-alert.yaml
```

Verifique os recursos:

```bash
kubectl get pods
kubectl get svc
kubectl get hpa
```

No Windows, o script `k8s/start-k8s.bat` compila as imagens Docker locais e aplica os manifests em ordem.

## Publicando Imagens No Docker Hub

Faca login:

```bash
docker login
```

As imagens do projeto estao configuradas para o usuario Docker Hub `thuliott` e publicadas para `linux/amd64`:


| Servico        | Imagem                                     |
| -------------- | ------------------------------------------ |
| Config Server  | `thuliott/saferoute-config-server:latest`  |
| Eureka Server  | `thuliott/saferoute-eureka-server:latest`  |
| API Gateway    | `thuliott/saferoute-api-gateway:latest`    |
| Sensor Service | `thuliott/saferoute-sensor-service:latest` |
| Alert Service  | `thuliott/saferoute-alert-service:latest`  |
| Jenkins        | `thuliott/saferoute-jenkins:latest`        |
| Simulator      | `thuliott/saferoute-simulator:latest`      |


Links:

- `https://hub.docker.com/r/thuliott/saferoute-config-server`
- `https://hub.docker.com/r/thuliott/saferoute-eureka-server`
- `https://hub.docker.com/r/thuliott/saferoute-api-gateway`
- `https://hub.docker.com/r/thuliott/saferoute-sensor-service`
- `https://hub.docker.com/r/thuliott/saferoute-alert-service`
- `https://hub.docker.com/r/thuliott/saferoute-jenkins`
- `https://hub.docker.com/r/thuliott/saferoute-simulator`

Para regenerar e publicar as imagens a partir dos Dockerfiles locais:

```bash
docker buildx build --platform linux/amd64 -t thuliott/saferoute-config-server:latest --push ./services/config-server
docker buildx build --platform linux/amd64 -t thuliott/saferoute-eureka-server:latest --push ./services/eureka-server
docker buildx build --platform linux/amd64 -t thuliott/saferoute-api-gateway:latest --push ./services/api-gateway
docker buildx build --platform linux/amd64 -t thuliott/saferoute-sensor-service:latest --push ./services/sensor-service
docker buildx build --platform linux/amd64 -t thuliott/saferoute-alert-service:latest --push ./services/alert-service
docker buildx build --platform linux/amd64 -t thuliott/saferoute-jenkins:latest --push ./jenkins
docker buildx build --platform linux/amd64 -t thuliott/saferoute-simulator:latest --push ./simulator
```

Para baixar e executar as imagens publicadas sem rebuild local:

```bash
docker compose pull
docker compose up -d
```

Os manifests em `k8s/` tambem apontam para as imagens publicadas, por exemplo:

```yaml
image: thuliott/saferoute-sensor-service:latest
```

Se as imagens forem privadas, configure um `imagePullSecret` no cluster.

## Estrutura Do Projeto

```text
.
├── Jenkinsfile
├── docker-compose.yml
├── prometheus.yml
├── simulator.py
├── jenkins/
├── k8s/
└── services/
    ├── alert-service/
    ├── api-gateway/
    ├── config-server/
    ├── eureka-server/
    └── sensor-service/
```

## Comandos Uteis

Logs de um servico:

```bash
docker compose logs -f sensor-service
```

Rebuild de um servico:

```bash
docker compose build sensor-service
docker compose up -d sensor-service
```

Status dos containers:

```bash
docker compose ps
```

Limpar ambiente local:

```bash
docker compose down -v
```

## Uso De IA

O projeto teve apoio de IA de forma transparente.

Modelos/ferramentas utilizados:

- OpenAI Codex
- Claude Code

Para que a IA foi usada:

- Criar a base deste `README.md`.
- Ajustar nomes das imagens Docker Hub para o usuario `thuliott`.
- Verificar se o `jenkins/Dockerfile` estava correto para criar o Jenkins em container com Maven, Python, plugins e Configuration as Code.
- Apoiar na criacao do arquivo `simulator.py` para envio de telemetrias em massa ao API Gateway.
- Revisar scripts, comandos de validacao e boas praticas de Docker Compose.

Exemplos reais de prompts usados:


| Prompt                                                                                                                                                                                    | Resposta                                                                                                |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `Crie um README completo para este projeto, incluindo instalacao, execucao com Docker Compose, uso da API, Jenkins, Kubernetes, publicacao no Docker Hub e transparencia sobre uso de IA` | Geracao inicial do README, depois revisada e expandida                                                  |
| `Gere um simulador em Python para enviar varias telemetrias para o endpoint /sensor/telemetry do API Gateway`                                                                             | Criacao da base do `simulator.py`, depois ajustada para o formato de dados usado pelo SafeRoute         |
| `Verifique se o Dockerfile do Jenkins esta correto para rodar o pipeline em container com Maven, Python, plugins e JCasC`                                                                 | Revisao do `jenkins/Dockerfile` e confirmacao dos pontos necessarios para o Jenkins executar o pipeline |
| `Adicione os links das imagens no readme`                                                                                                                                                 | Os links das imagens Docker foram adicionados ao README                                                 |


Respostas aceitas, ajustadas ou descartadas:

- Aceitas: estrutura do README, comandos de validacao, base do simulador, publicacao da imagem Jenkins e inclusao dos links do Docker Hub.
- Ajustadas: textos do README, nomes das imagens, variaveis de ambiente, simulador de telemetria e explicacoes para ficarem coerentes com o projeto.
- Descartadas: sugestoes genericas que nao refletiam a ideia inicial

Dinamica de uso:

- A IA foi usada como apoio de revisao, desenvolvimento, documentacao, debug e verificacao.
- As mudancas foram conferidas com comandos locais como `docker compose build`, `mvn verify` e `docker manifest inspect`.

Partes desenvolvidas manualmente pelo grupo:

- Definicao do dominio do sistema SafeRoute e regras de alerta de temperatura.
- Implementacao dos microsservicos Spring Boot.
- Organizacao da arquitetura com Config Server, Eureka, API Gateway, banco, observabilidade e CI.
- Revisao final das sugestoes de IA antes de entrar no repositorio.
