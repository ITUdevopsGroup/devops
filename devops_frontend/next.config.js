const os = require("os");

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  env: {
    host: "http://ec2-13-60-32-149.eu-north-1.compute.amazonaws.com",
    port: "5001",
    portClient: "3000",
  },
};

module.exports = nextConfig;
