import React, { useState, useEffect } from 'react';

export function NewPost() {

  const [errorMessage, setErrorMessage] = useState('');
  const [post, setPost] = useState('');
  let postData = JSON.parse(localStorage.getItem('samplePostData'))
  let postList = postData["posts"]

  const handlePostChange = (event) => {
    setPost(event.target.value);
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
  
    let text = document.getElementById('post-input');

    if(!localStorage.getItem("currentUser")){
      setErrorMessage('You must log in to post')
    }else if(text.value === ''){
      setErrorMessage('Your post cannot be empty')
    }else{

      let data={
        "user":localStorage.getItem("currentUser"),
        "message":text.value,
        "id":postList.length+1,
        "comment_id":1,
        "comments": []
      }

      try{
        const response = await fetch('/api/post', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data)
        })
        
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }

        localStorage.removeItem("samplePostData")
        localStorage.removeItem("dataLoaded")
        window.location.href = "/"
      }catch(error){
        console.error('There was a problem with your fetch operation:', error);

      }

    }
  };

  return (
    <main>
      <h1>New Post</h1>
      <form id="new-post" className="register-form" onSubmit={handleSubmit}>
          <textarea id="post-input" name="post-input" rows="10" cols="50" value={post} onChange={handlePostChange}></textarea>
          <br></br>
          <input type="submit" className="new-post-button" value='Submit'/>
      </form>
      <div id="errormessage"><p className="form-content">{errorMessage}</p></div>
    </main>
  );
}
