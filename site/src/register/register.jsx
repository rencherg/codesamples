import React, { useState } from 'react';

export function Register() {

  const [errorMessage, setErrorMessage] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [zip, setZip] = useState('');

  const handleUsernameChange = (event) => {
    setUsername(event.target.value);
  }

  const handlePasswordChange = (event) => {
    setPassword(event.target.value);
  }

  const handleConfirmPasswordChange = (event) => {
    setConfirmPassword(event.target.value);
  }

  const handleZipChange = (event) => {
    setZip(event.target.value);
  }
  
  const handleSubmit = async (event) => {
    event.preventDefault();
   
    const regex = /^\d{5}$/;

    if ((username === '') || (password === '') || (confirmPassword === '') || (zip === '')) {
      setErrorMessage('Please fill out all fields');
    } else if (!(password === confirmPassword)) {
      setErrorMessage('Passwords do not match');
    } else if (!(regex.test(zip))) {
      setErrorMessage('Zip Code must be a five digit number');
    } else {
      const data = {
        username: username,
        password: password,
        zipcode: zip,
        'lat/long': '<latlong here>',
        authToken: ''
      };

      fetch('/api/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
      }).then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      }).then(responseBody => {
        if (responseBody.message === "success") {
          localStorage.removeItem("loggedIn");
          localStorage.removeItem("dataLoaded");
          localStorage.setItem("loggedIn", true);
          localStorage.setItem("currentUser", responseBody.username);
          localStorage.setItem("zipcode", responseBody.userZip);
          window.location.href = "/";
        } else {
          setErrorMessage(responseBody.message);
        }

      })
      .catch(error => {
        console.error('There was a problem with your fetch operation:', error);
      });
    }
  };

  return (
    <main>
      <h1>Register</h1>
      <form id="register-form" onSubmit={handleSubmit}>
          <div className="register-form">
              <label htmlFor="username">Username</label><br></br>
              <input type="text" id="username-input" name="username" value={username} onChange={handleUsernameChange}></input><br></br>
              <label htmlFor="zip">Zip Code</label><br></br>
              <input type="number" id="zip" name="zip" minLength="5" maxLength="5" value={zip} onChange={handleZipChange}></input><br></br>
              <label htmlFor="password">Password</label><br></br>
              <input type="password" id="password" name="password" value={password} onChange={handlePasswordChange}></input><br></br>
              <label htmlFor="password-confirm">Confirm password</label><br></br>
              <input type="password" id="password-confirm" name="password-confirm" value={confirmPassword} onChange={handleConfirmPasswordChange}></input><br></br>
              <input type="submit" className="form-button" value="Submit"></input>
          </div>
      </form>
      <div id="error-message"><p className="form-content">{errorMessage}</p></div>
    </main>
  );
}
