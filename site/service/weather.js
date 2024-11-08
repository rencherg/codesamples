async function getWeather(zipCode){
    const weatherApiKey = 'e351ed4582104db6b1310847241303';
    const url = `http://api.weatherapi.com/v1/current.json?key=${weatherApiKey}&q=${zipCode}&units=imperial`;
    
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const responseData = await response.json();
        const weatherData = {
            "city": responseData.location.name,
            "temperature": responseData.current.temp_f
        };
        return weatherData;
    } catch (error) {
        console.error('Error fetching weather data:', error);
        throw error;
    }
}

module.exports = getWeather