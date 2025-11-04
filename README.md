# 🏦💬 Budget Management System

A **comprehensive AI-powered budget management platform** built with cutting-edge technologies including Spring AI, Anthropic Claude, Model Context Protocol (MCP), and React. This system demonstrates advanced microservices architecture with conversational AI integration for natural language budget management.

📖 **Read the full tutorial on Medium:** [Building a Smart Budget Assistant with MCP, React, and Spring Boot](https://medium.com/@ayari.mehdi.93/building-a-smart-budget-assistant-with-mcp-react-and-spring-boot-the-ai-revolution-you-can-5cb5f4dd130a)

## 💬 See It In Action

![Budget Chat Assistant Demo](images/demo_final.gif)

*Chat with your budget like a human. No forms, no buttons — just natural conversation powered by Claude AI.*

## 🌟 System Overview

The Budget Management System consists of **three interconnected applications** that work together to provide a seamless AI-enhanced budget management experience:

1. **🏦 Budget MCP Server** - Backend data management with MCP tools
2. **🤖 Budget MCP Client** - AI integration layer with Claude
3. **💬 Budget Chat Frontend** - React-based conversational UI

Users can manage their budgets through natural language conversations with an AI assistant, which executes operations on the backend through the Model Context Protocol.

## 🏗️ Complete System Architecture

![Budget Management System Global Architecture](images/budget-system-global-architecture.png)

### 🔄 Communication Flow

1. **User Input** → Frontend captures natural language input
2. **HTTP Request** → Frontend sends message to MCP Client via REST API
3. **AI Processing** → Claude processes the request and determines required tools
4. **MCP Tool Execution** → Client calls MCP Server tools via MCP protocol
5. **Database Operations** → Server executes budget operations on H2 database
6. **Response Chain** → Results flow back through the layers to the user

## 🛠️ Technology Stack Summary

### **Backend Technologies**
| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Framework** | Spring Boot | 3.5.7 | Microservices foundation |
| **Language** | Java | 21 | Programming language |
| **AI Integration** | Spring AI | 1.1.0-M3 | AI framework with MCP enhancements |
| **AI Provider** | Anthropic Claude | Sonnet 4 | Large language model |
| **Database** | H2 Database | 2.3.232 | File-based persistent storage |
| **ORM** | Spring Data JPA | 3.5.7 | Data access |
| **Reactive** | Spring WebFlux | 6.2.7 | Non-blocking I/O |
| **Protocol** | MCP | 1.0 (SDK 0.14.0) | AI-tool communication |

### **Frontend Technologies**
| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Framework** | React | 19.1.0 | UI library |
| **Language** | TypeScript | 4.9.5 | Type safety |
| **Styling** | Tailwind CSS | 3.4.17 | CSS framework |
| **Icons** | Lucide React | 0.511.0 | Icon library |
| **Markdown** | React Markdown | 10.1.0 | Rich text rendering |
| **Build Tool** | React Scripts | 5.0.1 | Build system |

### **DevOps & Tools**
| Component | Technology | Purpose |
|-----------|------------|---------|
| **Build** | Maven | Java dependency management |
| **Package Manager** | npm | Node.js packages |
| **Containerization** | Docker | Application packaging |
| **Monitoring** | Spring Actuator | Health checks & metrics |
| **Testing** | JUnit + Jest | Testing frameworks |

## 📁 Project Structure

```
budget-management-system/
├── 📊 budget-mcp-server/              # MCP Server (Port 8081)
│   ├── src/main/java/com/budgetserver/
│   │   ├── 🏗️ entity/                 # JPA Entities
│   │   │   ├── Budget.java
│   │   │   ├── Transaction.java
│   │   │   └── TransactionType.java
│   │   ├── 📦 repository/             # Data Access Layer
│   │   │   ├── BudgetRepository.java
│   │   │   └── TransactionRepository.java
│   │   ├── ⚙️ service/                # Business Logic & MCP Tools
│   │   │   └── BudgetMcpService.java
│   │   ├── 📈 actuator/               # Custom Endpoints
│   │   │   └── McpToolsEndpoint.java
│   │   └── 🚀 BudgetMcpServerApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml            # Server Configuration
│   ├── Dockerfile                     # Container Config
│   └── pom.xml                        # Maven Dependencies
│
├── 🤖 budget-mcp-client/              # MCP Client (Port 8080)
│   ├── src/main/java/com/budgetclient/
│   │   ├── 🌐 controller/             # REST Controllers
│   │   │   └── ChatController.java
│   │   ├── ⚙️ service/                # Business Services
│   │   │   ├── ChatService.java
│   │   │   └── BudgetSummaryService.java
│   │   ├── 🔧 config/                 # Configuration
│   │   │   ├── ChatClientConfig.java
│   │   │   └── ChatMemoryConfig.java
│   │   ├── 📋 dto/                    # Data Transfer Objects
│   │   │   ├── ChatRequest.java
│   │   │   ├── ChatResponse.java
│   │   │   └── BudgetSummary.java
│   │   └── 🚀 BudgetMcpClientApplication.java
│   ├── src/main/resources/
│   │   └── application.yml            # Client Configuration
│   └── pom.xml                        # Maven Dependencies
│
├── 💬 budget-chat-frontend/           # React Frontend (Port 3000)
│   ├── src/
│   │   ├── 🧩 components/             # React Components
│   │   │   ├── Chat/                  # Chat Interface
│   │   │   │   ├── ChatArea.tsx
│   │   │   │   ├── ChatInput.tsx
│   │   │   │   └── ChatMessage.tsx
│   │   │   ├── Sidebar/               # Sidebar Components
│   │   │   │   ├── QuickStats.tsx
│   │   │   │   ├── QuickActions.tsx
│   │   │   │   └── ExampleCommands.tsx
│   │   │   ├── Layout/                # Layout Components
│   │   │   │   └── Header.tsx
│   │   │   └── UI/                    # Base UI Components
│   │   │       ├── Button.tsx
│   │   │       └── LoadingSpinner.tsx
│   │   ├── 🪝 hooks/                  # Custom Hooks
│   │   │   └── useChat.ts
│   │   ├── 🌐 services/               # API Services
│   │   │   └── api.ts
│   │   ├── 📝 types/                  # TypeScript Types
│   │   │   ├── api.ts
│   │   │   └── index.ts
│   │   ├── 🛠️ utils/                  # Utility Functions
│   │   │   └── formatters.ts
│   │   └── App.tsx                    # Main App Component
│   ├── package.json                   # npm Dependencies
│   ├── tailwind.config.js             # Tailwind Configuration
│   └── Dockerfile                     # Container Config
│
├── 📚 README.md                       # This file
└── 📄 Individual README files         # Component-specific docs
```

## 🚀 Complete System Setup

### Prerequisites

- ☕ **Java 21** or higher
- 📦 **Maven 3.6+**
- 📦 **Node.js 16+** and npm
- 🔑 **Anthropic API Key** ([Get one here](https://console.anthropic.com/))
- 🐳 **Docker** (optional, for containerized deployment)

### ⚠️ Important Security Notes

**Before running the application:**
1. **NEVER commit your API keys** to version control
2. Use environment variables for sensitive data
3. The `.env` file is ignored by git - keep it local
4. Review the `.env.example` file for required configuration

### Method 1: Local Development Setup

#### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd budget-management-system
```

#### Step 2: Set Up Environment Variables

**Option 1: Using .env file (Recommended)**
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env and add your Anthropic API key
nano .env  # or use your preferred editor
```

Your `.env` file should contain:
```bash
ANTHROPIC_API_KEY=your-anthropic-api-key-here
ALLOWED_ORIGINS=http://localhost:3000
```

**Option 2: Export environment variables**
```bash
export ANTHROPIC_API_KEY=your_api_key_here
export ALLOWED_ORIGINS=http://localhost:3000
```

**Option 3: IDE Configuration**
- In IntelliJ IDEA: Run → Edit Configurations → Environment Variables
- Add: `ANTHROPIC_API_KEY=your_api_key_here`

#### Step 3: Start the MCP Server (Terminal 1)
```bash
cd budget-mcp-server
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
🔍 **Verify**: `curl http://localhost:8081/actuator/health`

💡 **Note**:
- Use `-Dspring-boot.run.profiles=dev` for development (in-memory DB)
- Use `-Dspring-boot.run.profiles=prod` for production (file-based DB)
- Default profile uses file-based persistent storage in `./data/` directory

#### Step 4: Start the MCP Client (Terminal 2)
```bash
cd budget-mcp-client
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
🔍 **Verify**: `curl http://localhost:8080/actuator/health`

💡 **Note**: Make sure `ANTHROPIC_API_KEY` environment variable is set!

#### Step 5: Start the Frontend (Terminal 3)
```bash
cd budget-chat-frontend
npm install
npm start
```
🔍 **Verify**: Open `http://localhost:3000`

### Method 2: IntelliJ IDE Setup

#### Step 1: Open Project in IntelliJ
1. Open IntelliJ IDEA
2. File → Open → Select `budget-management-system` folder
3. Trust the project when prompted

#### Step 2: Configure Run Configurations
1. **Budget MCP Server**:
   - Run → Edit Configurations
   - Add → Spring Boot
   - Main class: `com.budgetserver.BudgetMcpServerApplication`
   - Module: `budget-mcp-server`

2. **Budget MCP Client**:
   - Add → Spring Boot
   - Main class: `com.budgetclient.BudgetMcpClientApplication`
   - Module: `budget-mcp-client`
   - Environment variables: `ANTHROPIC_API_KEY=your_key`

#### Step 3: Run Applications
1. Start `BudgetMcpServerApplication` first
2. Start `BudgetMcpClientApplication` second
3. Open terminal and run frontend:
   ```bash
   cd budget-chat-frontend
   npm install && npm start
   ```

## 🧪 System Testing

### End-to-End Testing Flow

#### 1. Health Checks
```bash
# Check all services are running
curl http://localhost:8081/actuator/health  # MCP Server
curl http://localhost:8080/actuator/health  # MCP Client
curl http://localhost:3000                  # Frontend
```

#### 2. API Testing
```bash
# Test chat functionality
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Create a budget for food with $500 limit"}'

# Test budget summary
curl http://localhost:8080/api/budget/summary
```

#### 3. Frontend Testing
1. Open browser to `http://localhost:3000`
2. Try these commands in the chat:
   ```
   Create a budget for groceries with $500 monthly limit
   Add expense of $45.50 for grocery shopping in food category
   Show me all my budgets
   How much have I spent this month?
   Get my recent transactions
   ```

## 📊 Monitoring & Health

### Health Endpoints
- **MCP Server**: `http://localhost:8081/actuator/health`
- **MCP Client**: `http://localhost:8080/actuator/health`
- **Frontend**: `http://localhost:3000` (React dev server)

### Database Console
- **H2 Console**: `http://localhost:8081/h2-console` (dev profile only)
  - **Development (in-memory)**: `jdbc:h2:mem:budgetdb`
  - **Production (file-based)**: `jdbc:h2:file:./data/budgetdb`
  - Username: `sa`
  - Password: (empty)

💡 **Note**: In production profile, H2 console is disabled for security

### Monitoring Endpoints
- **Server Metrics**: `http://localhost:8081/actuator/metrics`
- **Client Metrics**: `http://localhost:8080/actuator/metrics`
- **MCP Tools Info**: `http://localhost:8081/actuator/mcp-tools`

## 🔧 Configuration Profiles

The system supports multiple Spring profiles for different environments:

### Available Profiles

| Profile | Use Case | Database | Logging | H2 Console |
|---------|----------|----------|---------|------------|
| **dev** | Development | In-memory H2 | DEBUG/TRACE | Enabled |
| **default** | Local testing | File-based H2 | DEBUG | Enabled |
| **prod** | Production | File-based H2 or PostgreSQL | INFO/WARN | Disabled |

### Using Profiles

**Maven:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**JAR:**
```bash
java -jar -Dspring.profiles.active=dev target/budget-mcp-server-0.0.1-SNAPSHOT.jar
```

**Docker:**
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

### Profile-Specific Configuration

**Development (`dev`):**
- In-memory database (data lost on restart)
- Verbose logging
- H2 console enabled
- CORS allows all origins
- Schema auto-drop and recreate

**Production (`prod`):**
- File-based persistent database
- Minimal logging (INFO level)
- H2 console disabled
- CORS restricted to configured domains
- Schema update only (no data loss)
- Log files with rotation

## 🔐 Security Best Practices

### For Development
✅ Use `.env` file for API keys

✅ Never commit `.env` to git

✅ Use `dev` profile for testing

✅ Keep dependencies updated

### For Production
✅ Use `prod` profile

✅ Set strong database passwords

✅ Restrict CORS origins

✅ Enable HTTPS

✅ Disable H2 console

✅ Use PostgreSQL instead of H2

✅ Implement authentication/authorization

✅ Monitor logs and metrics

✅ Use secrets management (Vault, AWS Secrets Manager)

### Environment Variables Reference

```bash
# Required
ANTHROPIC_API_KEY=your-api-key-here

# Optional (with defaults)
ALLOWED_ORIGINS=http://localhost:3000
DB_URL=jdbc:h2:file:./data/budgetdb
DB_USERNAME=sa
DB_PASSWORD=
```

## 🐛 Troubleshooting

### Common Issues

| Issue | Symptoms | Solution |
|-------|----------|----------|
| **Port conflicts** | `Address already in use` | Kill processes on ports 3000, 8080, 8081 |
| **API key missing** | `Authentication failed` | Set `ANTHROPIC_API_KEY` environment variable |
| **Database errors** | `Connection refused` | Restart MCP Server, check H2 configuration |
| **MCP connection failed** | `MCP server unavailable` | Ensure MCP Server started before Client |
| **Frontend API errors** | `Network Error` | Verify MCP Client is running on port 8080 |
| **Build failures** | `Compilation errors` | Run `mvn clean install` and `npm install` |

## 🌟 Key Features Showcase

### 💬 Conversational AI
- Natural language budget management
- Context-aware responses with memory
- Rich markdown formatting with emojis

### 🔧 MCP Integration
- Real-time tool execution
- Structured data exchange
- Event-driven communication

### 📱 Modern UI/UX
- Responsive design for all devices
- Intuitive chat interface
- Real-time updates and feedback

### 🏗️ Microservices Architecture
- Clear separation of concerns
- Scalable and maintainable design
- Independent deployment capabilities


## 🎉 Getting Started

Ready to explore AI-powered budget management? Follow the setup instructions above and start chatting with your budget assistant! 

For detailed component-specific information, check the individual README files in each project folder.

