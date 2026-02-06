'use client'

import Image from "next/image";
import { useEffect } from "react";
import { useState } from 'react';



var host = "http://desktop-h4nlgfr"
var port = "5001"

interface message {
  id: any;
  author_id: any;
  text: any;
  pub_date: string;
  flagged: string;
  username: string;
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
    <div className="public">
      Public Timeline
      <ul>
        {items?.map((item) => (
          <li key={item.id}>
                <img src="{{ message.email|gravatar(size=48) }}"></img>
                <p>
                  <strong><a href="{{ url_for('user_timeline', username=message.username)
                  }}">{item.username }</a></strong>
                </p>
                { item.text}
                  <small>&mdash; {item.pub_date }</small>
                
              </li>
        ))}
    </ul>
      
    </div>
  );
}
