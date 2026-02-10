'use client'

import Image from "next/image";
import styles from './messages.module.css'
import { useEffect } from "react";
import { useState } from 'react';
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
  const params = useSearchParams()
  const router = useRouter()
  var refetch =  true

  
  let g = {user:params.get("user"),username:params.get("username")}
  console.log("user " +  g)
  let loggedIn = g.user !=null ? true : false
  useEffect(() => {
    // console.log("refetch " + items.length + " user: "+ refetch)
    if(items.length == 0  && (userId != undefined || userId == null) && refetch) {
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

  let title = userId == undefined ? "Public Timeline" : username + "'s Timeline"
  let itemsPresent = items != undefined ? true : false

 return (
    <div>
      <h1>MiniTwit</h1> 
      <div className="navigation">
        {loggedIn ? (
          <p> 
            <strong><a title="" onClick={() => update(g.user, g.username)}>my timeline</a></strong>
            <strong><a title="" onClick={() => update(undefined, undefined)}>public timeline</a></strong><br />
            <strong><a title="" onClick={() => route(router,"/logout")}>sign out</a></strong>
          </p>
        ) : (
          <p>
            <strong><a title="" onClick={() => update(undefined, undefined)}>public timeline</a></strong><br />
            <strong><a title="" onClick={() => route(router,"/register?user=" + g.user + "&username="+g.username)}>sign up</a></strong> <br />
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
