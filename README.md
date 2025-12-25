# Banking Assistant Chatbot 
The Banking Chatbot is a backend-focused conversational system designed to assist bank customers in real time with account inquiries, transaction summaries, transfer guidance, and frequently asked questions (FAQ).
The system uses WebSocket (STOMP) for real-time communication and optionally supports AI-powered responses.

## Project Overview
This project provides:

- Real-time customer–chatbot communication

- Fast and secure access to banking information

- Rule-based and optional AI-powered responses

- A scalable backend architecture built with Spring Boot

## Technologies Used

- Java 17+

- Spring Boot 3

- Spring WebSocket + STOMP

- Spring Data JPA

- PostgreSQL

- Redis (optional)

- Spring Security & JWT (optional)

- AI Integration (optional – OpenAI / LLM(I used the qwen2.5-3b-instruct-q4_k_m.gguf llm model in my project))

## Features
### Real-Time Chat

- Instant messaging via WebSocket

- STOMP protocol support

- Multiple concurrent chat sessions

### Chatbot Capabilities
#### Account Information

- Current balance

- Recent transactions

- Account details (type, IBAN)

#### Transaction Guidance

- Money transfers

- Deposits and withdrawals

- Step-by-step banking instructions

#### FAQ

- Branch and ATM information

- Working hours

- Common banking questions

#### AI Support

- Intelligent responses for unrecognized queries

- Dynamic natural language interaction

#### Admin and Monitoring

- Monitoring active chat sessions

- Chat history logging

- Data analysis and auditing support

## Database Design

### PostgreSQL:

- customers

- accounts

- transactions

- chat_logs

- transactions

### Redis (optional):

- Chat session caching

- Rate limiting

- Frequently used responses

## WebSocket Configuration
```WebSocket Endpoint: /ws/chat
Application Destination Prefix: /app
Topics:
  /topic/public
```
## Project Structure
src/main/java<br>
 └── BankingAssistantChatbot<br>
     ├── controller<br>
     ├── service<br>
     ├── repository<br>
     ├── model<br>
     ├── dto<br>
     ├── config<br>
     └── security<br>
     
##  Screenshots
- CHATBOT TR
  <img width="1328" height="852" alt="image" src="https://github.com/user-attachments/assets/2d534e46-3878-4370-b44d-cb4792aca075" />
  <img width="1221" height="857" alt="image" src="https://github.com/user-attachments/assets/4ec22cca-4487-4c66-abf2-2ec3210f2bb5" />
- CHATBOT ENG
  <img width="1377" height="837" alt="3" src="https://github.com/user-attachments/assets/7cc0093b-ad1e-4cfb-a094-ad83059180ab" />
  <img width="1265" height="867" alt="4" src="https://github.com/user-attachments/assets/add80f87-0ced-4c02-a51b-f83e9c874191" />
- JWT Token Test
  <img width="1920" height="1028" alt="JWTTokenTesting" src="https://github.com/user-attachments/assets/4a74d970-e465-43a3-95a7-78af25478647" />
  

## How to Run the Application
```
git clone https://github.com/nurdankrhn/BankingAssistantChatbot.git
cd BankingAssistantChatbot
./mvnw spring-boot:run
```
