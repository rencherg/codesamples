import { useEffect } from 'react';

export function sampleData() {
  useEffect(() => {
    async function loadData() {
      if (!localStorage.getItem("dataLoaded")){

        try{
          const response = await fetch('/api/data', {
              method: 'GET',
          })
  
          if (!response.ok) {
              throw new Error('Network response was not ok');
          }
      
          const responseData = await response.json()
  
          localStorage.setItem("samplePostData", JSON.stringify(responseData.samplePostData));
          localStorage.setItem("dataLoaded", true);
          localStorage.setItem("sampleWebsocketData", JSON.stringify(responseData.sampleWebsocketData));

        } catch(error) {
          console.error('There was a problem with your fetch operation:', error);
        };
    }
    }

    loadData();
  }, []);

}