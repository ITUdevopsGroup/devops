'use client'

import Image from "next/image";
import styles from './messages.module.css'
import { useEffect } from "react";
import { useState,FormEvent } from 'react';
import Gravatar from 'react-gravatar'
import { useRouter,useSearchParams,usePathname   } from 'next/navigation';

var host = process.env.host
var port = process.env.port
interface message {
  messageId: any;
  authorId: any;
  text: any;
  pubDate: string;
  flagged: string;
  userId: string;
  username: string;
  email: string;
  pwHash: string;
}

function gravatar_url(email : string, size=80) {
  let emailFormat = email.trim().toLowerCase() + "?d=identicon&s="+ size
  let result = "'http://www.gravatar.com/avatar/" +  emailFormat ;
  return result
}

export default function Timeline() {
  
  const [items, setItems] = useState(Array<message>);
  const [refetchNew, setRefetch] = useState(true);
  const [userId, setUserId] = useState(undefined);
  const [username, setUsername] = useState(undefined);
  const [timelineText, setTimelineText] = useState("public");
  const [timeline, setTimeline] = useState("");
  const [followed, setFollowed] = useState(false);
  const params = useSearchParams()
  const router = useRouter()
  var refetch =  true
  
  let session = {user:params.get("user"),username:params.get("username")}
  let loggedIn = session.user !=null ? true : false
  useEffect(() => {
    if(items != null &&  items.length == 0  && (userId != undefined || userId == null) && refetch) {
      getPublicTimeLine() 
      refetch = false
    }
    else if((userId == undefined || userId == null) && refetch) {
      getPublicTimeLine()
    refetch = false}
    else if((userId != undefined || userId != null) && refetch) {
      getUserTimeLine()
    refetch = false}
  },[refetchNew]);
    
  function update(userId:any,username:any) {
    setUserId(userId)
    setUsername(username)
    setRefetch(refetchNew ? false : true)
    if(username == undefined) setTimelineText("public")
    else if(username != session.username) setTimelineText("user_timeline")
    else setTimelineText("timeline")
    console.log("session " + session.username + " user " + username + " timeline " + timelineText)


}

  function route(router:any,path:string) {
    setRefetch(refetchNew ? false : true)
    setItems([])
    router.push(path)
}

  async function getPublicTimeLine(){
    refetch = false
    let api = await fetch(host +":" + port)
    let apijson = await api.json()
    setItems(apijson.data);
  }
    async function getUserTimeLine(){
    let api = await fetch(host +":" + port + "/user?user=" + userId)
    let apijson = await api.json()
    setItems(apijson.data);
  }

  function follow(username:any) {
    alert("You are now following " + username)
  }
  function unfollow(username:any) {
    alert("You are no longer following " + username)

  }
  async function addMessage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const formData = new FormData(event.currentTarget)
    var text = formData.get('text')
    alert('Your message was recorded')
    console.log("text "+ text)
  }

  let title = timelineText == "public" ? "Public Timeline" : timelineText == "user_timeline" ? username + "'s Timeline" : "My timeline"
  let subTitle = timelineText == "user_timeline" ? session.username == username ? 
    <div>This is you! <br /></div> : 
      followed ? 
        <div>You are currently following this user<p> <a className="unfollow" title="" onClick={() => unfollow(username)}> Unfollow user</a></p></div> : 
        <div>You are not yet following this user<a className="follow" title="" onClick={() => follow(username)}>Follow user</a>.</div> : ""

  let addMessageView = timelineText == "timeline" ? 
        <div className="twitbox">
        <h3>What's on your mind { session.username }?</h3>
        <form onSubmit={addMessage}>
            <dl>
                <dd><input type="text" name="text" size={60} ></input></dd>
            </dl>
            <div className="twitbox"><input type="submit" value= "Share"></input> </div>
        </form>
      </div>
  : ""

  let itemsPresent = items != undefined ? true : false

  function logout() {
    session.user = null
    session.username = null
    setUserId(undefined)
    setUsername(undefined)
    setRefetch(refetchNew ? false : true)
    setItems([])
    router.push("/timeline")

  }

 return (
    <div>
      <h1>MiniTwit</h1> 
      <div className="navigation">
        {loggedIn ? (
          <p> 
            <strong><a title="" onClick={() => update(session.user, session.username)}>my timeline</a></strong><br />
            <strong><a title="" onClick={() => update(undefined, undefined)}>public timeline</a></strong><br />
            <strong><a title="" onClick={() => logout()}>sign out</a></strong>
          </p>
        ) : (
          <p>
            <strong><a title="" onClick={() => update(undefined, undefined)}>public timeline</a></strong><br />
            <strong><a title="" onClick={() => route(router,"/register?user=" + session.user + "&username="+session.username)}>sign up</a></strong> <br />
            <strong><a title="" onClick={() => route(router,"/login")}>sign in</a></strong> <br />
          </p>
        )}
      </div>
  {/* {% with flashes = get_flashed_messages() %}
    {% if flashes %}
      <ul className="flashes">
      {% for message in flashes %}
        <li>{{ message }}
      {% endfor %}
      </ul>
    {% endif %}
  {% endwith %} */}

      <h2>{title}</h2>
      <div>{subTitle}</div>
      <div>{addMessageView}</div>
      <ul className="messages">
        {itemsPresent ?  (items?.map((item) => (
          <li key={item.messageId}>
                <Gravatar email={gravatar_url(item.email)} />
                <p> <strong><a title="" onClick={() => update(item.userId, item.username)}>{item.username }</a></strong></p>

                { item.text}
                  <small>&mdash; {item.pubDate }</small>
                
              </li>
        ))) : (<li><em>There's no message so far.</em></li>)}
    </ul>
        <div className="footer">
    MiniTwit &mdash; A Flask Application
  </div>
    </div>
    );
  
}
