'use client'

import Image from "next/image";
import styles from './messages.module.css'
import { useEffect } from "react";
import { useState } from 'react';
import Gravatar from 'react-gravatar'



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
  

export default function Home() {
  const [items, setItems] = useState(Array<message>);
  const [refetch, setRefetch] = useState(false);

    useEffect(() => { 
    testfunc()
  }, [refetch]);

    async function testfunc(){
      let api = await fetch(host +":" + port)
      let apijson = await api.json()
      setItems(apijson.data);
      
      
  }
  console.log(items)

 return (
    <div className="messages">
      Public Timeline
      <ul className="messages">
        {items?.map((item) => (
          <li key={item.messageId}>
                <Gravatar email="{message.email}" />
                {/* <img src="{ message.email|gravatar(size=48) }"></img> */}
                <p>
                  <strong><a href="{{ url_for('user_timeline', username=message.username)
                  }}">{item.username }</a></strong>
                </p>
                { item.text}
                  <small>&mdash; {item.pubDate }</small>
                
              </li>
        ))}
    </ul>
      
    </div>
  );
}
