# GenAIRAGChatbot 🤖📄
A document-aware GenAI chatbot built using **Retrieval-Augmented Generation (RAG)** to provide grounded, context-aware responses from uploaded PDFs.

## 🔍 Overview
This project demonstrates how to build a production-ready RAG pipeline that integrates:
- **LangChain** for prompt orchestration
- **ChromaDB** for vector-based semantic search
- **OpenAI LLMs** for answer generation
- **FastAPI** backend + Streamlit UI for interactivity

          +----------------+          +---------------------+          +--------------------+
          |  Your Local PC |  Push    |     GitHub Repo     |  Triggers GitHub Actions CI |  
          +----------------+--------->| (GenAIRAGChatbot)   |----------------------------->|
                                        +---------------------+                            |
                                                                                           v
                                                                           +----------------------------------+
                                                                           | GitHub Actions Workflow (.yml)   |
                                                                           | - Build JAR with Gradle          |
                                                                           | - Build Docker image via Jib     |
                                                                           | - Push image to AWS ECR          |
                                                                           | - Deploy to ECS Fargate          |
                                                                           +----------------------------------+
                                                                                           |
                                                                                           v
                                                                          +------------------------------+
                                                                          | Amazon ECR (Elastic Registry)|
                                                                          |  - Stores container images   |
                                                                          +------------------------------+
                                                                                           |
                                                                                           v
                                                            +------------------------------------------------+
                                                            | AWS ECS Fargate (Cluster + Service + Task)    |
                                                            |  - Pulls new image from ECR                   |
                                                            |  - Deploys container behind Load Balancer     |
                                                            |  - Auto-scales + auto-rollback if fails       |
                                                            +------------------------------------------------+



## 🚀 Features
- 📄 PDF ingestion and chunking with metadata-aware splitting
- 🔍 Vector search powered by ChromaDB
- 🧠 Prompt templates with memory and contextual fallback
- ✅ Source-cited answers to reduce hallucinations
- 📊 Built-in hallucination evaluation framework (WIP)

## 🧪 Sample Usage

```text
Q: What is the refund policy in the attached document?
A: According to the policy section (Page 4), refunds are only issued within 15 days of purchase...

[Source: policy.pdf, chunk_32]
```

## 📦 Tech Stack

- Python, FastAPI, Streamlit
- LangChain, OpenAI, ChromaDB
- PyPDF for document parsing
- GitHub Actions for CI


## 🛠️ Developer Runbook

This section documents setup steps, deployment workflows, and common troubleshooting patterns during development of this RAG-based GenAI chatbot.


### ⚙️ Environment Setup

```bash
/usr/libexec/java_home -V     # List all installed JDKs
export JAVA_HOME="/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home"
echo $JAVA_HOME               # Confirm path
java -version                 # Confirm Java version
```

### 🧩 Gradle Configuration Fix

To silence native method warnings when running `./gradlew`:
gradle.properties - To overcome warnings

**Warning:**
```
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by net.rubygrapefruit.platform.internal.NativeLibraryLoader in an unnamed module (file:/Users/laxmankishorek/.gradle/wrapper/dists/gradle-8.10-bin/deqhafrv1ntovfmgh0nh3npr9/gradle-8.10/lib/native-platform-0.22-milestone-26.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled
```


✅ **Solution:** Add this to `gradle.properties`:

```properties
org.gradle.jvmargs=--enable-native-access=ALL-UNNAMED
```

### 🚀 Build & Run Commands

| Action          | Command                                                                 |
|----------------|--------------------------------------------------------------------------|
| Build Project   | `./gradlew clean build` (generates JAR in `build/libs/`)                |
| Run App         | `./gradlew bootRun`                                                     |
| Stop App        | `Ctrl + C` or `lsof -i :8080` → get PID → `kill -9 <pid>`               |
| Clean Rebuild   | `./gradlew clean build`                                                 |

### 🌐 GitHub Setup

1. Created local project
2. Created empty GitHub repo: `GenAIRAGChatbot`
3. Added remote origin:

```bash
git remote add origin https://github.com/laxmankishore/GenAIRAGChatbot.git
```
4. Committed and pushed:

```bash
git add .
git commit -m "Initial commit with deploy workflow"
git push origin main
```
### 🐳 Docker + Jib Deployment Notes

Used Jib to containerize the application.

**Default Base Image:** `eclipse-temurin` (formerly AdoptOpenJDK)
Jib uses eclipse-temurin (formerly AdoptOpenJDK) as the default base image — unless you explicitly override it.

**Issue:**
```bash
I/O error for image [registry-1.docker.io/library/eclipse-temurin]:
org.apache.http.ConnectionClosedException
Premature end of Content-Length delimited message body
```

✅ **Resolution:**

- Retry push after network stabilization
- Consider pinning image to a known-good SHA or using Amazon Corretto as base image


### 🧠 Why ECR Repo Must Exist Before Pushing
- AWS **does not auto-create** repositories like Docker Hub might.
- When Jib tries to push an image to:
  ```
  ***.dkr.ecr.us-east-2.amazonaws.com/genairagchatbot
  ```
  ...ECR must already have a **repo named `genairagchatbot`**, or the push will fail.
- ECR = your private Docker registry for storing versioned app containers.

### 💥 Jib Push Failure — Diagnosed
```text
I/O error for image [public.ecr.aws/amazoncorretto/amazoncorretto]
ConnectionClosedException
The repository 'genairagchatbot' does not exist
```
**Root causes:**
- Jib failed to pull the base image (network interruption)
- Target ECR repository didn’t exist at time of push

✅ Solution:
```bash
aws ecr create-repository --repository-name genairagchatbot --region us-east-2
```

### 🔁 Retrying Jib
Jib caches layers, so repeated attempts are efficient. After ECR is created, rerun:
```bash
./gradlew jib
```

---

### ⚙️ Why Jib Works Without Docker
- Jib doesn’t rely on Docker daemon or Dockerfiles.
- It builds the image **in-process**, using Java bytecode analysis and layering.
- Works well for CI/CD in environments without Docker (e.g., GitHub-hosted runners).

---

### 🧱 ECS Fargate Deployment Pipeline Recap
```text
Code → GitHub → Jib → ECR → ECS Fargate
```
- GitHub Actions pushes image to ECR using Jib
- ECS Fargate pulls the image and deploys a new task
- No EC2 or manual Docker builds needed

---

### 🛡️ Auto Rollbacks in ECS
Enabled via:
- CodeDeploy blue/green deployments
- CloudWatch alarms monitoring health
- If a deployment fails (e.g., container crashes), AWS automatically rolls back to previous task definition

---

## 🧠 Key Takeaways
- Always **create ECR repos** ahead of deployment
- Network issues may interrupt base image pulls — Jib is resilient with caching
- Docker is not needed on your machine with Jib
- Use **CloudWatch alarms + CodeDeploy** for safe production rollouts

---


## 🧭 Why We Use ECS with EC2 (Instead of Fargate)

To reduce costs and gain more control over the infrastructure, we use **Amazon ECS with EC2 launch type** instead of **Fargate**. Here's why:

### ✅ Cost Efficiency
| Launch Type | Free Tier Eligible | Notes |
|-------------|--------------------|-------|
| **Fargate** | ❌ No               | Billed per second for vCPU and RAM |
| **EC2**     | ✅ Yes              | 750 hours/month for `t2.micro` or `t3.micro` under AWS Free Tier |

Using ECS with EC2 helps us stay within the AWS Free Tier, which significantly reduces development and testing costs.

### 🔧 Infrastructure Control
- Full access to EC2 instances (via SSH)
- Can run multiple containers per instance
- Greater flexibility for monitoring, logging, and customization

### 🚀 Orchestration with ECS
- ECS handles task scheduling, service management, and deployments
- EC2 just serves as the container host
- This setup balances automation with control

### 📦 Reusable and Scalable
- Multiple services can share a single EC2 instance
- No per-container billing as in Fargate
- Easy to scale up later by adding instances or enabling auto-scaling

---

**Summary:**  
We chose ECS + EC2 for its **free-tier eligibility**, **cost-effectiveness**, and **flexibility**, while still benefiting from ECS's powerful orchestration features.














### 🧱 Next Steps (Planned Enhancements)

- [ ] Streamlit frontend for interactive chat UI
- [ ] LLM output logging + retrieval grounding scores
- [ ] Multimodal extension (VLM/image-aware prompts)

## 🧠 Why This Matters

This project serves as a foundation for deploying reliable GenAI systems in production by focusing on:

- Grounded retrieval
- Prompt strategy design
- Evaluation of LLM outputs

## 👤 Author

**Laxman Kishore Koppisetti**  
[LinkedIn](https://www.linkedin.com/in/laxmankishore) | [GitHub](https://github.com/laxmankishore)

Chat : https://chatgpt.com/c/6892763f-a2ac-832d-aa8a-6ea60fabedd7

