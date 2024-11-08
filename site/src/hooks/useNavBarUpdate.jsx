import { useEffect, useState } from 'react';

export function useNavBarUpdate() {
  const [loginTitle, setLoginTitle] = useState('');
  const [tempTitle, setTempTitle] = useState('');
  const [username, setUsername] = useState('');

  useEffect(() => {
    async function updateNavBar() {
      try {
        if (localStorage.getItem('loggedIn')) {
          setLoginTitle('Logout');
          setUsername(localStorage.getItem('currentUser'));

          if (!localStorage.getItem('temp')) {
            localStorage.setItem('temp', await getTemp());
          }

          const temp = localStorage.getItem('temp');
          if (temp !== undefined) {
            setTempTitle(temp + '°');
          }
        } else {
          const response = await fetch('/api/authenticate', {
            method: 'POST',
          });

          if (!response.ok) {
            throw new Error('Network response was not ok');
          }

          const responseBody = await response.json();

          if (responseBody.message === 'success') {
            localStorage.setItem('loggedIn', true);
            localStorage.setItem('currentUser', responseBody.username);
            localStorage.setItem('zipcode', responseBody.userZip);

            setLoginTitle('Logout');
            setUsername(localStorage.getItem('currentUser'));

            const temp = await getTemp();
            if (temp !== undefined) {
              setTempTitle(temp + '°');
            }
            localStorage.setItem('temp', temp);
          } else {
            setLoginTitle('Login');
            setUsername('');
          }
        }
      } catch (error) {
        console.error('There was a problem with your fetch operation:', error);
        throw error;
      }
    }

    updateNavBar();
  }, []);

  async function getTemp() {
    const zipCode = localStorage.getItem('zipcode');
    const url = '/api/weather/' + zipCode;

    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const responseData = await response.json();
      return responseData.temperature;
    } catch (error) {
      console.error('Error fetching weather data:', error);
      throw error;
    }
  }

  return { loginTitle, tempTitle, username };
}