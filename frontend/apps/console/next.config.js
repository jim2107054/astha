/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  transpilePackages: ['@astha/ui', '@astha/i18n', '@astha/api-client', '@astha/mocks'],
};

module.exports = nextConfig;
