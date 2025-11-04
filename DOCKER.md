# Docker Setup - Budget Management System

## Quick Start

1. **Set your API key:**
   ```bash
   echo "ANTHROPIC_API_KEY=your_actual_api_key" > .env
   ```

2. **Start everything:**
   ```bash
   # Windows
   docker-compose up --build
   ```

3. **Access the applications:**
   - Frontend: http://localhost:3000
   - API (MCP Client): http://localhost:8080
   - MCP Server: http://localhost:8081

## Stop Services

```bash
docker-compose down
```

## Clean Everything

```bash
docker-compose down
docker system prune -a
```

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   MCP Client    │    │   MCP Server    │
│   (React)       │───▶│   (Spring AI)   │───▶│   (Budget API)  │
│   Port: 3000    │    │   Port: 8080    │    │   Port: 8081    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Troubleshooting

- **Build fails?** Check internet connection for Docker image downloads
- **API key error?** Update `.env` file with valid Anthropic API key
- **Port conflicts?** Stop other services using ports 3000, 8080, 8081
