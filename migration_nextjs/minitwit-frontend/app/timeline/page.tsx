'use client'

import Image from "next/image";
import styles from './messages.module.css'
import { useEffect } from "react";
import { useState } from 'react';
import Gravatar from 'react-gravatar'
import { useRouter,useSearchParams,usePathname   } from 'next/navigation';
import { log } from "console";





var host = "http://desktop-h4nlgfr"
var port = "5001"

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
  // setItems([])
  const router = useRouter()
  const params = useSearchParams()
  const path = usePathname()
  const userId =  params.get("user")
  const username =  params.get("username")
  var refetch =  params.get("refetch") == "true" ? true : false
  
  let g = {user:"",username:""}
  let loggedIn = g.user != "" ? true : false
  
  // let refetch = false;
  console.log("refetch " + items.length + " user: "+ refetch)
  useEffect(() => {
    console.log("refetch " + items.length + " user: "+ refetch)
    if(items.length == 0  && userId == null && refetch) {
      getPublicTimeLine() 
      refetch = false
    }
   
    else if(userId == undefined || userId == null && refetch) {
      getPublicTimeLine()
    refetch = false}
    else if(items.length == 0 && refetch) {
      getPublicTimeLine()
    refetch = false}
    else if(userId != undefined || userId != null && refetch) {
      getUserTimeLine()
    refetch = false}
  },[items]);

   

    


  function route(router:any,path:string) {
    refetch = false
    // setRefetch(true)
    // setItems([])
    // refetch = true
    
    router.push(path)
}

  async function getPublicTimeLine(){
    refetch = false
    let api = await fetch(host +":" + port)
    console.log("public")
    let apijson = await api.json()
    setItems(apijson.data);
    // setRefetch(true)
    // refetch = false
  }
    async function getUserTimeLine(){
    let api = await fetch(host +":" + port + "/user?user=" + userId)
    let apijson = await api.json()
    console.log("user")
    setItems(apijson.data);
  }

  let title = userId == undefined ? "Public Timeline" : username + "'s Timeline"
  // let followStatus = 
  let itemsPresent = items != undefined ? true : false

 return (
    <div>

      <h1>MiniTwit</h1>
  <div className="navigation">
  {loggedIn ? (
    <p> 
      <strong><a title="" onClick={() => route(router,"/timeline?user=" + g.user + "&username="+ g.username + "&refetch=true")}>my timeline</a></strong>
      <strong><a title="" onClick={() => route(router,"/timeline")}>public timeline</a></strong>
      <strong><a title="" onClick={() => route(router,"/logout")}>sign out</a></strong>
    </p>
  ) : (
    <p>
      <strong><a title="" onClick={() => route(router,"/timeline")}>public timeline</a></strong> <br />
      <strong><a title="" onClick={() => route(router,"/register")}>sign up</a></strong> <br />
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
      <ul className="messages">
        {itemsPresent ?  (items?.map((item) => (
          <li key={item.messageId}>
                <Gravatar email={gravatar_url(item.email)} />
                <p> <strong><a title="" onClick={() => route(router,"/timeline?user=" + item.userId + "&username="+ item.username + "&refetch=true")}>{item.username }</a></strong></p>
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
