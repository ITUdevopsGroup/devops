'use client'

import Image from "next/image";
import * as http from 'http';
import { JSX, Key, SetStateAction,useEffect } from "react";
import { useState } from 'react';



var host = "172.30.160.1"
var path = ""
var preparePath = ""


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
      let api = await fetch('http://desktop-h4nlgfr:5001')
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
