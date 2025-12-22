import React, { useEffect, useMemo, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import axios from "axios";

const WS_URL = "http://localhost:8080/ws/chat";
const LOGIN_URL = "http://localhost:8080/auth/login";

// Put a REAL IBAN that exists in DB
const DEFAULT_IBAN = "TR12000620000000000000000001";

// Put the REAL ollama model name here (as seen in `ollama list`)
const DEFAULT_MODEL = "qwen2.5-3b-instruct-q4_k_m:latest";

export default function App() {
  const stompClientRef = useRef(null);

  const [connected, setConnected] = useState(false);
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);

  const [userIban, setUserIban] = useState(DEFAULT_IBAN);
  const [model, setModel] = useState(DEFAULT_MODEL);

  // Optional: keep a list for dropdown (you can add more later)
  const modelOptions = useMemo(() => [DEFAULT_MODEL], []);

  useEffect(() => {
    const socket = new SockJS(WS_URL);

    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 2000,
      debug: () => {}, // turn on if needed: (str) => console.log(str)
      onConnect: () => {
        setConnected(true);

        // Subscribe to backend broadcast
        client.subscribe("/topic/public", (frame) => {
          try {
            const body = JSON.parse(frame.body);
            setMessages((prev) => [...prev, body]);
          } catch {
            // if backend sends plain text
            setMessages((prev) => [
              ...prev,
              { sender: "SYSTEM", content: frame.body, type: "SYSTEM" },
            ]);
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers["message"]);
        console.error("Details:", frame.body);
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      try {
        client.deactivate();
      } catch {}
    };
  }, []);

  const sendMessage = () => {
    const client = stompClientRef.current;
    if (!client || !client.active || !connected) {
      alert("WebSocket not connected yet.");
      return;
    }

    const trimmed = message.trim();
    if (!trimmed) return;

    client.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({
        sender: userIban,
        content: trimmed,
        type: "USER",
        // IMPORTANT: send the REAL model name (NOT sha256 blob)
        model: model,
      }),
    });

    setMessage("");
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    sendMessage();
  };

  const handleLogin = async () => {
    try {
      const res = await axios.post(LOGIN_URL, {
        email: "john.doe@example.com",
        password: "dummyPassword123",
      });
      console.log("JWT:", res.data);
      alert("Login request sent. Check console for JWT.");
    } catch (err) {
      console.error("Login failed:", err?.response?.data || err.message);
      alert("Login failed. Check console.");
    }
  };

  return (
    <div style={{ maxWidth: 820, margin: "24px auto", padding: 16, fontFamily: "Arial" }}>
      <h1 style={{ marginBottom: 8 }}>Banking Chatbot (Test UI)</h1>

      <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 16 }}>
        <span
          style={{
            padding: "6px 10px",
            borderRadius: 8,
            color: "#fff",
            background: connected ? "green" : "gray",
            fontSize: 12,
          }}
        >
          {connected ? "Connected" : "Disconnected"}
        </span>

        <button onClick={handleLogin} type="button">
          Login (optional)
        </button>
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          gap: 12,
          marginBottom: 16,
        }}
      >
        <div>
          <label style={{ display: "block", fontSize: 12, marginBottom: 6 }}>Sender IBAN</label>
          <input
            value={userIban}
            onChange={(e) => setUserIban(e.target.value)}
            style={{ width: "100%", padding: 10 }}
            placeholder="TR..."
          />
        </div>

        <div>
          <label style={{ display: "block", fontSize: 12, marginBottom: 6 }}>Model</label>
          <select
            value={model}
            onChange={(e) => setModel(e.target.value)}
            style={{ width: "100%", padding: 10 }}
          >
            {modelOptions.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div
        style={{
          border: "1px solid #ddd",
          borderRadius: 10,
          padding: 12,
          height: 340,
          overflowY: "auto",
          marginBottom: 16,
          background: "#fafafa",
        }}
      >
        {messages.length === 0 ? (
          <div style={{ color: "#666" }}>No messages yet.</div>
        ) : (
          messages.map((m, i) => (
            <div key={i} style={{ marginBottom: 10 }}>
              <div style={{ fontSize: 12, color: "#555" }}>
                <strong>{m.sender || "UNKNOWN"}</strong>{" "}
                {m.type ? <span style={{ opacity: 0.7 }}>({m.type})</span> : null}
              </div>
              <div style={{ whiteSpace: "pre-wrap" }}>{m.content}</div>
            </div>
          ))
        )}
      </div>

      <form onSubmit={handleSubmit} style={{ display: "flex", gap: 10 }}>
        <input
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Ask something… (Enter to send)"
          style={{ flex: 1, padding: 12 }}
        />
        <button type="submit" disabled={!connected} style={{ padding: "12px 16px" }}>
          Send
        </button>
      </form>

      <div style={{ marginTop: 10, fontSize: 12, color: "#666" }}>
        Tip: if you see no response, check backend logs + confirm Ollama endpoint works:
        <code style={{ marginLeft: 6 }}>POST http://localhost:11434/api/chat</code>
      </div>
    </div>
  );
}
