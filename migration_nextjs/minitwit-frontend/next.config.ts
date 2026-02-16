import type { NextConfig } from "next";
const os = require('os');

const nextConfig: NextConfig = {
  reactStrictMode: false,
  env: {
    host: "http://localhost", //+ os.hostname(),
    port : "5001",
    portClient : "3000"
  }
};

export default nextConfig;
