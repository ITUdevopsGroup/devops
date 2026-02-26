"use client";

import Image from "next/image";

import { useEffect, useState } from "react";
import Gravatar from "react-gravatar";
import { useRouter, useSearchParams, usePathname } from "next/navigation";

const host = process.env.NEXT_PUBLIC_API_HOST;
const port = process.env.NEXT_PUBLIC_API_PORT;

function gravatar_url(email, size = 80) {
  let emailFormat = email.trim().toLowerCase() + "?d=identicon&s=" + size;
  let result = "'http://www.gravatar.com/avatar/" + emailFormat;
  return result;
}

var addMessageText = "";

export default function Timeline() {
  const [items, setItems] = useState([]);
  const [apiData, setApiData] = useState();
  const [dataFollow, setDataFollow] = useState();
  const [dataUnFollow, setDataUnFollow] = useState();
  const [dataAddMessage, setDataAddMessage] = useState();
  const [shouldAddMessage, setShouldAddMessage] = useState(false);
  const [dataIsFollowed, setDataIsFollowed] = useState();
  const [shouldRefetch, setShouldRefetch] = useState(true);
  const [userId, setUserId] = useState(undefined);
  const [username, setUsername] = useState(undefined);
  const [subTitle, setSubTitle] = useState();
  const [timelineText, setTimelineText] = useState("public");
  const [timeline, setTimeline] = useState("");
  const [followed, setFollowed] = useState(false);
  const [shouldFollow, setShouldFollow] = useState(false);
  const [shouldUnFollow, setShouldUnFollow] = useState(false);

  const params = useSearchParams();
  const router = useRouter();
  var refetch = true;

  let session = { user: params.get("user"), username: params.get("username") };
  let loggedIn = session.user != null ? true : false;

  useEffect(() => {
    if (
      items != null &&
      items.length == 0 &&
      (userId != undefined || userId == null) &&
      refetch
    ) {
      getPublicTimeLine();
      refetch = false;
    } else if ((userId == undefined || userId == null) && refetch) {
      getPublicTimeLine();
      refetch = false;
    } else if ((userId != undefined || userId != null) && refetch) {
      getUserTimeLine();
      refetch = false;
    }
  }, [shouldRefetch]);

  function update(userId, username) {
    setUserId(userId);
    setUsername(username);
    setShouldRefetch(shouldRefetch ? false : true);

    if (username == undefined) setTimelineText("public");
    else if (username != session.username) setTimelineText("user_timeline");
    else setTimelineText("timeline");
  }

  function route(router, path) {
    setShouldRefetch(shouldRefetch ? false : true);
    setItems([]);
    router.push(path);
  }

  async function getPublicTimeLine() {
    refetch = false;
    console.log(host + ":" + port);
    let api = await fetch(host + ":" + port);
    let apijson = await api.json();
    setItems(apijson.data);
  }

  async function getUserTimeLine() {
    let api = await fetch(
      host + ":" + port + "/user?user=" + session.user + "&profile=" + username,
    );
    let apijson = await api.json();
    setItems(apijson.data);
    setFollowed(apijson.followed);
  }

  function follow(username) {
    setShouldFollow(true);
  }

  useEffect(() => {
    if (dataFollow?.result) {
      setFollowed(true);
      alert("You are now following " + username);
    } else if (dataFollow?.error) {
      alert("Something went wrong w. following " + username);
    }
  }, [dataFollow]);

  useEffect(() => {
    if (shouldFollow) {
      followReq("follow");
      setShouldFollow(false);
    }
  }, [shouldFollow]);

  async function followReq(followReq) {
    let api = await fetch(
      host +
        ":" +
        port +
        "/" +
        followReq +
        "?user=" +
        session.user +
        "&profile=" +
        username,
    );
    let apijson = await api.json();
    if (followReq == "follow") setDataFollow(apijson.userData);
    else if (followReq == "unfollow") setDataUnFollow(apijson.userData);
  }

  function unfollow(username) {
    setShouldUnFollow(true);
  }

  useEffect(() => {
    if (dataUnFollow?.result) {
      setFollowed(false);
      if (followed) alert("You are no longer following " + username);
    } else if (dataUnFollow?.error) {
      alert("Something went wrong w. unfollow " + username);
    }
  }, [dataUnFollow]);

  useEffect(() => {
    if (shouldUnFollow) {
      followReq("unfollow");
      setShouldUnFollow(false);
    }
  }, [shouldUnFollow]);

  // duplicated in original TS code - kept as-is (same behavior)
  useEffect(() => {
    if (dataUnFollow?.result) {
      setFollowed(false);
      if (followed) alert("You are no longer following " + username);
    } else if (dataUnFollow?.error) {
      alert("Something went wrong w. unfollow " + username);
    }
  }, [dataUnFollow]);

  useEffect(() => {
    if (shouldUnFollow) {
      followReq("unfollow");
      setShouldUnFollow(false);
    }
  }, [shouldUnFollow]);

  async function addMessage(event) {
    event.preventDefault();

    const formData = new FormData(event.currentTarget);
    addMessageText = formData.get("text");
    setShouldAddMessage(true);
  }

  useEffect(() => {
    if (dataAddMessage?.result) {
      alert("Your message was recorded");
      setShouldAddMessage(false);
      setDataAddMessage(undefined);
      setShouldRefetch(shouldRefetch ? false : true);
    } else if (dataAddMessage?.error) {
      alert("Something went wrong while posting the message");
    }
  }, [shouldAddMessage]);

  useEffect(() => {
    if (shouldAddMessage) {
      addMessageRequest(session.user, addMessageText, false);
    }
  }, [shouldAddMessage]);

  let title =
    timelineText == "public"
      ? "Public timeline"
      : timelineText == "user_timeline"
        ? username
        : "My timeline";

  function evaluateTimeline() {
    if (
      timelineText == "user_timeline" &&
      session.username == username &&
      session.username != null
    ) {
      setSubTitle(
        <div>
          This is you! <br />
        </div>,
      );
    } else if (
      followed &&
      timelineText == "user_timeline" &&
      session.username != username &&
      session.username != null
    ) {
      setSubTitle(
        <div>
          You are currently following this user
          <p>
            {" "}
            <a className="unfollow" title="" onClick={() => unfollow(username)}>
              {" "}
              Unfollow user
            </a>
          </p>
        </div>,
      );
    } else if (
      !followed &&
      timelineText == "user_timeline" &&
      session.username != username &&
      session.username != null
    ) {
      setSubTitle(
        <div>
          You are not yet following this user
          <a className="follow" title="" onClick={() => follow(username)}>
            Follow user
          </a>
          .
        </div>,
      );
    } else {
      setSubTitle("");
    }
  }

  useEffect(() => {
    evaluateTimeline();
  }, [shouldRefetch]);

  useEffect(() => {
    evaluateTimeline();
  }, [followed]);

  let addMessageView =
    timelineText == "timeline" ? (
      <div className="twitbox">
        <form onSubmit={addMessage}>
          <dl>
            <dd className="px-1 ">
              <textarea
                className="w-full h-32 rounded-xl border border-gray-300 px-3 py-2 align-top resize-none outline-none  focus:ring-2 focus:ring-[#66d6cb]"
                name="text"
                placeholder="What's on your mind?"
              ></textarea>
            </dd>
          </dl>
          <div className="">
            <input
              className="mt-2 mb-4 ml-1 !rounded-md w-30 h-10"
              type="submit"
              value="Share"
            ></input>{" "}
          </div>
        </form>
      </div>
    ) : (
      ""
    );

  let itemsPresent = items != undefined ? true : false;

  function logout() {
    session.user = null;
    session.username = null;
    setUserId(undefined);
    setUsername(undefined);
    setShouldRefetch(shouldRefetch ? false : true);
    setItems([]);
    router.push("/");
  }

  async function addMessageRequest(user, text, flagged) {
    const pubDate = Math.floor(Date.now() / 1000);
    let api = await fetch(
      host +
        ":" +
        port +
        "/add_message?user=" +
        encodeURIComponent(user) +
        "&text=" +
        encodeURIComponent(text) +
        "&pubDate=" +
        pubDate +
        "&flagged=" +
        flagged,
    );
    let apijson = await api.json();
    setDataAddMessage(apijson.userData);
    setShouldAddMessage(false);
  }

  return (
    <div className="w-full justify-center flex p-4 sm:p-10 ">
      <div className="lg:w-1/2 w-full bg-white py-6 px-4 lg:px-10 rounded-3xl">
        <div className="justify-start flex ">
          <h1 className=" rounded-sm text-4xl font-semibold  mt-4 text-[#377c72]">
            MiniTwit
          </h1>
        </div>
        <div className="mt-2 text-lg  ">
          {loggedIn ? (
            <div className="flex ">
              <strong className="mr-4 cursor-pointer">
                <a
                  title=""
                  onClick={() => update(session.user, session.username)}
                >
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    My timeline
                  </div>
                </a>
              </strong>
              <br />
              <strong className="mr-4 cursor-pointer">
                <a title="" onClick={() => update(undefined, undefined)}>
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    Public timeline
                  </div>
                </a>
              </strong>
              <br />
              <strong className="mr-4 cursor-pointer">
                <a title="" onClick={() => logout()}>
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    Sign out
                  </div>
                </a>
              </strong>
            </div>
          ) : (
            <div className="flex ">
              <strong className="mr-4 cursor-pointer ">
                <a title="" onClick={() => update(undefined, undefined)}>
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    Public timeline
                  </div>
                </a>
              </strong>
              <br />
              <strong className="mr-4 cursor-pointer">
                <a
                  title=""
                  onClick={() =>
                    route(
                      router,
                      "/register?user=" +
                        session.user +
                        "&username=" +
                        session.username,
                    )
                  }
                >
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    Sign up
                  </div>
                </a>
              </strong>{" "}
              <br />
              <strong className="mr-4 cursor-pointer">
                <a title="" onClick={() => route(router, "/login")}>
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    Sign in
                  </div>
                </a>
              </strong>{" "}
              <br />
            </div>
          )}
        </div>

        <h2 className="text-2xl font-medium mb-4 mt-12 text-[#377c72]">
          {title}
        </h2>
        <div>{subTitle}</div>
        <div>{addMessageView}</div>
        <ul className="messages">
          {itemsPresent ? (
            items?.map((item) => (
              <li
                className="bg-gray-100 rounded-xl mb-4  p-2 items-start"
                key={item.messageId}
              >
                <div className="flex items-start">
                  <div className="w-12 h-12 mr-4 bg-blue-100 shrink-0 overflow-hidden rounded">
                    <Gravatar email={gravatar_url(item.email)} size={60} />
                  </div>

                  <div className="min-w-0  justify-center">
                    <a
                      title=""
                      className="text-base  justify-top flex cursor-pointer "
                      onClick={() => update(item.userId, item.username)}
                    >
                      {item.username}
                    </a>

                    <div className="text-xs  font-extralight">
                      {" "}
                      {item.pubDate}
                    </div>
                  </div>
                </div>
                <div className="text-lg mt-2"> {item.text}</div>
              </li>
            ))
          ) : (
            <li>
              <em>There's no message so far.</em>
            </li>
          )}
        </ul>
      </div>
    </div>
  );
}
