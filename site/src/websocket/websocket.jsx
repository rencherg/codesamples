import React, { useEffect, useRef } from 'react';

function Websocket() {
  const registerFormRef = useRef(null);

  useEffect(() => {
    const registerForm = registerFormRef.current;
    const protocol = window.location.protocol === 'http:' ? 'ws' : 'wss';
    const socket = new WebSocket(`${protocol}://${window.location.host}/ws`);

    socket.onopen = (event) => {
      appendMsg('Websocket connected');
    };

    socket.onmessage = async (event) => {
      const text = await event.data;
      console.log(text);
      appendMsg(text);
    };

    socket.onclose = (event) => {
      appendMsg('Websocket disconnected');
    };

    function appendMsg(msg) {
      let pTag = document.createElement("p");
      pTag.textContent = msg;
      registerForm.appendChild(pTag);
    }

    return () => {
      socket.close();
    };
  }, []);

  return (
    <main>
      <h1>All Recent Comments(Websocket)</h1>
      <div className="websocket-content" ref={registerFormRef}></div>
    </main>
  );
}

export default Websocket;
