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

function processData( data: string) {
        console.log("data")
        console.log(JSON.parse(data).data)
        return JSON.parse(data).data
}

function get(callback: { (value: SetStateAction<boolean>): void; (arg0: boolean): void; }): JSX.Element[] {
    let opts = {
        'host': host,
        'port': 5001,
        'path': `/${path}`
    };
    var result:Array<message> = []
    var listItems:JSX.Element[] = []
    http.get(opts, (r: http.IncomingMessage): void => {
        let data = '';
        r.on('data', (chunk: string): void => {
            data += chunk;
            
        });
        r.on('end', (): JSX.Element[] =>{
            console.log('Response has ended');
            
            result = JSON.parse(data).data
            console.log(result);
            listItems = result.map((message) =>
            <li key={message.id}>
              <img src="{{ message.email|gravatar(size=48) }}"></img>
              <p>
                <strong><a href="{{ url_for('user_timeline', username=message.username)
                }}">{message.username }</a></strong>
              </p>
              { message.text}
                <small>&mdash; {message.pub_date }</small>
              
            </li>
            ); 
            callback(false)
            //console.log(listItems);
            
            return listItems

        });
        r.on('error', (err): void => {
            console.log('Following error occured during request:\n');
            console.log(err);
        });
        
    }).end();
   
    
    return listItems
}
