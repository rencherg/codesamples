const config = require('./dbConfig.json');
const url = `mongodb+srv://${config.userName}:${config.password}@${config.hostname}`;
const { MongoClient, ObjectId } = require('mongodb');

const postDataId = '660361f2fef954de8f3df06e'
const userDataId = '6603618bfef954de8f3df06d'
const websocketDataId = '660362a1fef954de8f3df06f'

let sampleUsers = {
    "userlist":[
    {
        "username":"rencherg",
        "password":"password",
        "zipcode":84003,
        "lat/long":"<latlong here>",
        "authToken":""
    },
    {
        "username":"foxmulder",
        "password":"password",
        "zipcode":20535,
        "lat/long":"<latlong here>",
        "authToken":""
    },
    {
        "username":"dscully",
        "password":"password",
        "zipcode":20535,
        "lat/long":"<latlong here>",
        "authToken":""
    },
    {
        "username":"wskinner",
        "password":"password",
        "zipcode":20535,
        "lat/long":"<latlong here>",
        "authToken":""
    },
    {
        "username":"test",
        "password":"test",
        "zipcode":84003,
        "lat/long":"<latlong here>",
        "authToken":""
    }
]}

const emptyUserList = {
    "userlist":[]
}

let samplePostData = {
    "posts":[
    {
        "user":"rencherg",
        "message":"This is my sample post, feel free to make a comment",
        "id":1,
        "comment_id":15,
        "comments": [
            {
                "user":"rencherg",
                "message":"Dang that's crazy!",
                "id":4,
                "subcomments":[
                    {
                        "user":"rencherg",
                        "message":"I guess so I mean he didn't even say what happened",
                        "id":5,
                        "subcomments":[]
                    },
                    {
                        "user":"rencherg",
                        "message":"Everyone has a story",
                        "id":6,
                        "subcomments":[
                            {
                                "user":"rencherg",
                                "message":"The indent means that this comment is replying to the one above it",
                                "id":7,
                                "subcomments":[]
                            },
                            {
                                "user":"rencherg",
                                "message":"Another comment",
                                "id":8,
                                "subcomments":[]
                            },
                            {
                                "user":"rencherg",
                                "message":"Another comment",
                                "id":9,
                                "subcomments":[
                                    {
                                        "user":"rencherg",
                                        "message":"Another comment",
                                        "id":10,
                                        "subcomments":[
                                            {
                                                "user":"rencherg",
                                                "message":"Another comment",
                                                "id":11,
                                                "subcomments":[
                                                    {
                                                        "user":"rencherg",
                                                        "message":"Another comment",
                                                        "id":12,
                                                        "subcomments":[]
                                                    },
                                                    {
                                                        "user":"rencherg",
                                                        "message":"Late",
                                                        "id":13,
                                                        "subcomments":[]
                                                    },
                                                    {
                                                        "user":"rencherg",
                                                        "message":"Bom dia",
                                                        "id":14,
                                                        "subcomments":[]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            {
                "user":"rencherg",
                "message":"It's a busy world huh",
                "id":2,
                "subcomments":[]
            },
            {
                "user":"rencherg",
                "message":"This is another comment",
                "id":3,
                "subcomments":[]
            },
            {
                "user":"rencherg",
                "message":"Another comment",
                "id":1,
                "subcomments":[]
            }
        ]
    },
    {
        "user":"test",
        "message":"Test data Test data Test data Test data Test data Test data",
        "id":2,
        "comment_id":2,
        "comments": [
            {
                "user":"rencherg",
                "message":"Test data",
                "id":1,
                "subcomments":[]
            }
        ],
    }
]}

let sampleWebsocketData = {
    "data":[
        "comment", "comment2", "more sample comments", "another", "ok", "Another sample comment", "late", "Bom dia"
    ]
}

function addToWebsocket(comment){
    sampleWebsocketData['data'].push(comment)
}

async function addToUsers(obj){
    sampleUsers['userlist'].push(obj)
}

function addPost(obj){
    samplePostData['posts'].push(obj)
}

function processComment(postData, commentText, currentPostID, parentID){

    let desiredPost = ''
    let postIndex

    //find correct post
    for(let i = 0; i < postData["posts"].length; i++){
        if(postData["posts"][i]["id"].toString()===currentPostID.toString()){

            desiredPost = postData["posts"][i]
            postIndex = i
        }
    }

    //If we are replying to the main post
    if(parentID === 'post-content'){

        postData["posts"][postIndex]['comments'].push({
            "user":desiredPost['user'],
            "message":commentText,
            "id":desiredPost['comment_id'],
            "subcomments":[]
        })

        postData['posts'][postIndex]['comment_id'] = postData['posts'][postIndex]['comment_id']+1

    }

    //Update desired parent comment tree
    for(let i = 0; i < desiredPost['comments'].length; i++){
        let updatedPost = addComment(desiredPost['comments'][i], parentID, desiredPost['comment_id'], desiredPost['user'], commentText)
        if(updatedPost!== null){

            desiredPost['comments'][i] = updatedPost
            postData['posts'][postIndex]['comment_id'] = postData['posts'][postIndex]['comment_id']+1
            postData['posts'][postIndex] = desiredPost

        }
    }

    return postData
}

//helper function for process comment
function addComment(comment, parentID, newID, user, newMessage){
    if(comment['id'].toString() === parentID){
        comment['subcomments'].push({
            "user":user,
            "message":newMessage,
            "id":newID,
            "subcomments":[]
        })
        return comment
    }else if(comment['subcomments'].length > 0){
        for(let i = 0; i < comment['subcomments'].length; i++){
            let result = addComment(comment['subcomments'][i], parentID, newID, user, newMessage)
            if(result !== null){
                comment['subcomments'][i] = result
                return comment
            }
        }
        return null
    }else{
        return null
    }
}

async function getAllItems() {

    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    let data = {}

    try {
        await client.connect();
        const collection = db.collection(collectionName);
        const postData = await collection.findOne({ _id: new ObjectId(postDataId) });
        const websocketData = await collection.findOne({ _id: new ObjectId(websocketDataId) });

        data = {
            "samplePostData": {
                "posts": postData.posts
            },
            "sampleWebsocketData": {
                "data": websocketData.data
            }
        }

    } catch (error) {
        console.error("Error retrieving items:", error);
    } finally {
        await client.close();
        return data
    }
}

async function getAllUsers(){
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    userData = []

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        const response = await collection.findOne({ _id: new ObjectId(userDataId) });

        userData = response.userlist

    } catch (error) {
        console.error("Error retrieving items:", error);
    } finally {
        await client.close();
        return userData
    }
}

async function addToWebsocketDb(newString) {
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        await collection.updateOne(
            { "_id": new ObjectId(websocketDataId) },
            { $push: { "data": newString } }
        );

    } catch (error) {
        console.error('Error updating document:', error);
    } finally {
        await client.close();
    }
}

async function addPostDb(obj) {
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        await collection.updateOne(
            { "_id": new ObjectId(postDataId) },
            { $push: { "posts": obj } }
        );

    } catch (error) {
        console.error('Error updating document:', error);
    } finally {
        await client.close();
    }
}

async function addUserDb(obj) {
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        await collection.updateOne(
            { "_id": new ObjectId(userDataId) },
            { $push: { "userlist": obj } }
        );

    } catch (error) {
        console.error('Error updating document:', error);
    } finally {
        await client.close();
    }
}

async function updateUserAuthToken(username, newToken) {
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        const userData = await collection.findOne({ _id: new ObjectId(userDataId) });

        const user = userData.userlist.find(user => user.username === username);

        if (user) {
            user.authToken = newToken;
        } else {
            console.error('User not found.');
            return;
        }

        await collection.updateOne({ _id: userData._id }, { $set: { userlist: userData.userlist } })

    } catch (error) {
        console.error('Error updating user authToken:', error);
    } finally {
        await client.close();
    }
}

async function updatePostData(newPostData) {
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        await collection.updateOne(
            { _id: new ObjectId(postDataId) },
            { $set: newPostData }
        );

    } catch (error) {
        console.error('Error replacing document:', error);
    } finally {
        await client.close();
    }
}

async function updateUserData(newUserData) {
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        await collection.updateOne(
            { _id: new ObjectId(userDataId) },
            { $set: newUserData }
        );

    } catch (error) {
        console.error('Error replacing document:', error);
    } finally {
        await client.close();
    }
}

async function updateWebsocketData(newWebsocketData) {
    const client = new MongoClient(url);
    const db = client.db('app_data');
    const collectionName = 'app_data';

    try {
        await client.connect();

        const collection = db.collection(collectionName);

        await collection.updateOne(
            { _id: new ObjectId(websocketDataId) },
            { $set: newWebsocketData }
        );

    } catch (error) {
        console.error('Error replacing document:', error);
    } finally {
        await client.close();
    }
}

//Effectively resets the data in the db with the sample data
async function resetDb(){
    await updatePostData(samplePostData)
    await updateUserData(emptyUserList)
    await updateWebsocketData(sampleWebsocketData)
}

//These are both for testing
// let samplePost={
//     "user":"ya boy",
//     "message":"bom dia",
//     "id":"3",
//     "comment_id":1,
//     "comments": []
// }

// let sampleUser={
    
//     "username":"bud",
//     "password":"password",
//     "zipcode":84003,
//     "lat/long":"<latlong here>",
//     "authToken":""
    
// }

resetDb()

module.exports = {processComment, addToUsers, addPost, addToWebsocket, getAllItems, updateUserAuthToken, addUserDb, addPostDb, addToWebsocketDb, updatePostData, getAllUsers}