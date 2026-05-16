const { Client } = require("@stomp/stompjs");
const WebSocket = require("ws");

const url = "ws://localhost:9090/ws-native";
const jwt = process.env.JWT;

if (!jwt) {
  console.error("Missing JWT env var. Set: $env:JWT='<token>'");
  process.exit(1);
}

const client = new Client({
  webSocketFactory: () => new WebSocket(url),
  connectHeaders: {
    Authorization: `Bearer ${jwt}`,
  },
  debug: (str) => console.log(str),
  reconnectDelay: 0,
  onConnect: () => {
    console.log("STOMP CONNECTED");

    client.subscribe("/user/queue/notifications", (m) => {
      console.log("MESSAGE /user/queue/notifications:", m.body);
    });
  },
  onStompError: (frame) => {
    console.error("STOMP ERROR:", frame.headers["message"]);
    console.error(frame.body);
  },
  onWebSocketClose: () => console.log("WS CLOSED"),
  onWebSocketError: (e) => console.log("WS ERROR", e),
});

client.activate();