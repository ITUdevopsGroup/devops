import type { NextConfig } from "next";
const os = require('os');

const nextConfig: NextConfig = {
  reactStrictMode: false,
  env: {
    host: "http://" + os.hostname(),
    port : "5001"
  }
};

export default nextConfig;
