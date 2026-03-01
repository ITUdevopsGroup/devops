const os = require("os");

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  env: {
    host: "http://ec2-13-51-198-31.eu-north-1.compute.amazonaws.com",
    port: process.env.DEVOPS_BACKEND_PORT,
    portClient: "3000",
  },
};

module.exports = nextConfig;
