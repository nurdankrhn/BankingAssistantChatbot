import React, { useEffect, useRef, useState } from "react";
import axios from "axios";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

function App() {
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const stompClientRef = useRef(null);

  // Put a REAL IBAN that exists in DB
  const userIban = "TR12000620000000000000000001";

  useEffect(() => {
    const socket = new SockJS("http://localhost:8080/ws/chat");

    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 3000,
      debug: (str) => console.log(str),
      onConnect: () => {
        console.log("✅ STOMP connected");

        client.subscribe("/topic/public", (frame) => {
          const body = JSON.parse(frame.body);
          setMessages((prev) => [...prev, body]);
        });
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers["message"]);
        console.error("Details:", frame.body);
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => client.deactivate();
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!message.trim()) return;

    // publish to /app/chat.send (because prefix is /app and mapping is /chat.send)
    stompClientRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({
        sender: userIban,
        content: message,
        type: "USER",
      }),
    });

    setMessage("");
  };

  const handleLogin = async () => {
    try {
      const res = await axios.post("http://localhost:8080/auth/login", {
        email: "john.doe@example.com",
        password: "dummyPassword123",
      });
      console.log("JWT:", res.data);
    } catch (err) {
      console.error("Login failed:", err?.response?.data || err.message);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <h1>Banking Chatbot</h1>

      <button onClick={handleLogin}>Login</button>

      <h2>Messages:</h2>
      <div>
        {messages.map((m, i) => (
          <p key={i}>
            <strong>{m.sender}:</strong> {m.content}
          </p>
        ))}
      </div>

      <form onSubmit={handleSubmit}>
        <input
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Ask something..."
        />
        <button type="submit">Send</button>
      </form>
    </div>
  );
}

export default App;
