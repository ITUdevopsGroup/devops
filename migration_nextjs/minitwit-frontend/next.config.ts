import type { NextConfig } from "next";
const os = require('os');

const nextConfig: NextConfig = {
  reactStrictMode: false,
  env: {
    host: "http://ec2-13-51-198-31.eu-north-1.compute.amazonaws.com/" //+ os.hostname(),
    port : "5001",
    portClient : "3000"
  }
};

export default nextConfig;
