'use client'

import Image from "next/image";
import styles from './messages.module.css'
import { useEffect } from "react";
import { useState } from 'react';
import Gravatar from 'react-gravatar'
import { useRouter,useSearchParams  } from 'next/navigation';




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

function route(router:any,path:string) {
  console.log(path)
  router.push(path)
}


  

export default function Timeline() {
  
  const [items, setItems] = useState(Array<message>);
  const router = useRouter()
  const params = useSearchParams()

  const userId =  params.get("user")
  const username =  params.get("username")


  useEffect(() => { 
      userId == undefined ? getPublicTimeLine() : getUserTimeLine()
  }, [items]);

  async function getPublicTimeLine(){
    let api = await fetch(host +":" + port)
    let apijson = await api.json()
    setItems(apijson.data);
  }
    async function getUserTimeLine(){
    let api = await fetch(host +":" + port + "/user?user=" + userId)
    let apijson = await api.json()
    setItems(apijson.data);
  }

  let title = userId == undefined ? "Public Timeline" : username + "'s Timeline"
  // let followStatus = 

 return (
    <div>
      <h2>{title}</h2>
      <ul className="messages">
        {items.length != 0} {items?.map((item) => (
          <li key={item.messageId}>
                <Gravatar email={gravatar_url(item.email)} />
                <p> 
                  <strong><a href="#" onClick={() => route(router,"/timeline?user=" + item.userId + "&username="+ item.username)}>{item.username }</a></strong>
                </p>
                { item.text}
                  <small>&mdash; {item.pubDate }</small>
                
              </li>
        ))} : <li><em>There's no message so far.</em></li>
    </ul>
      
    </div>
  );
}
